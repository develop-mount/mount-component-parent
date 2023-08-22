package icu.develop.l2cache.interceptor;

import com.alibaba.fastjson.JSONObject;
import icu.develop.l2cache.L2Cache;
import icu.develop.l2cache.annotation.CacheKeyType;
import icu.develop.l2cache.constant.L2CacheConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.lang.Nullable;
import org.springframework.util.*;
import org.springframework.util.function.SingletonSupplier;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Description:
 *
 * @author linfeng
 * @version 1.0.0
 * @since 2023/8/12 21:03
 */
@Slf4j
public abstract class L2CacheAspectSupport implements BeanFactoryAware, SmartInitializingSingleton {

    public static final Object NO_RESULT = new Object();

    /**
     * Indicate that the result variable cannot be used at all.
     */
    public static final Object RESULT_UNAVAILABLE = new Object();

    private final Map<L2CacheOperationCacheKey, L2CacheOperationMetadata> metadataCache = new ConcurrentHashMap<>(1024);

    private BeanFactory beanFactory;

    private L2CacheOperationSource l2CacheOperationSource;

    public SingletonSupplier<L2CacheResolver> cacheResolver;

    private boolean initialized = false;

    public void configure(L2CacheOperationSource l2CacheOperationSource, Supplier<L2CacheResolver> cacheResolver) {
        this.l2CacheOperationSource = l2CacheOperationSource;
        this.cacheResolver = new SingletonSupplier<>(cacheResolver, cacheResolver);
    }

    public L2CacheResolver getCacheResolver() {
        return cacheResolver.get();
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Nullable
    protected Object execute(L2CacheOperationInvoker invoker, Object target, Method method, Object[] args) {
        // Check whether aspect is enabled (to cope with cases where the AJ is pulled in automatically)
        if (this.initialized) {
            Class<?> targetClass = getTargetClass(target);
            L2CacheOperationSource cacheOperationSource = getCacheOperationSource();
            if (cacheOperationSource != null && cacheOperationSource.isCandidateClass(targetClass)) {
                Collection<L2CacheOperation> operations = cacheOperationSource.getCacheOperations(method, targetClass);
                if (!CollectionUtils.isEmpty(operations)) {
                    return execute(invoker, method, new L2CacheOperationContexts(operations, method, args, target, targetClass));
                }
            }
        }

        return invoker.invoke();
    }


    @Nullable
    private Object execute(final L2CacheOperationInvoker invoker, Method method, L2CacheOperationContexts contexts) {

        // Process any early evictions
        processCacheEvicts(contexts.get(L2CacheEvictOperation.class), NO_RESULT, true);

        // Check if we have a cached item matching the conditions
        Object cacheHit = findCachedItem(contexts.get(L2CacheableOperation.class));

        // Collect puts from any @Cacheable miss, if no cached item is found
        List<L2CachePutRequest> l2CachePutRequests = new LinkedList<>();
        if (cacheHit == null) {
            collectPutRequests(contexts.get(L2CacheableOperation.class), NO_RESULT, l2CachePutRequests);
        }

        Object cacheValue;
        Object returnValue;

        if (cacheHit != null) {
            // If there are no put requests, just use the cache hit
            cacheValue = cacheHit;
            returnValue = wrapCacheValue(method, cacheValue);
        } else {
            // Invoke the method if we don't have a cache hit
            returnValue = invokeOperation(invoker);
            cacheValue = unwrapReturnValue(returnValue);
        }

        // Collect any explicit @CachePuts
        collectPutRequests(contexts.get(L2CachePutOperation.class), cacheValue, l2CachePutRequests);

        // Process any collected put requests, either from @CachePut or a @Cacheable miss
        for (L2CachePutRequest l2CachePutRequest : l2CachePutRequests) {
            l2CachePutRequest.apply(cacheValue);
        }

        // Process any late evictions
        processCacheEvicts(contexts.get(L2CacheEvictOperation.class), cacheValue, false);

        return returnValue;
    }

    private void collectPutRequests(Collection<L2CacheOperationContext> contexts,
                                    @Nullable Object result, Collection<L2CachePutRequest> putRequests) {

        for (L2CacheOperationContext context : contexts) {
            String key = generateKey(context, result);
            putRequests.add(new L2CachePutRequest(context, key));
        }
    }

    private void processCacheEvicts(
            Collection<L2CacheOperationContext> contexts, @Nullable Object result, boolean beforeInvocation) {

        for (L2CacheOperationContext context : contexts) {
            L2CacheEvictOperation operation = (L2CacheEvictOperation) context.metadata.operation;
            if (operation.isBeforeInvocation() == beforeInvocation) {
                performCacheEvict(context, operation, result);
            }
        }
    }

    private void performCacheEvict(
            L2CacheOperationContext context, L2CacheEvictOperation operation, @Nullable Object result) {

        String key = null;
        for (L2Cache cache : context.getCaches()) {
            if (operation.isCacheWide()) {
                doClear(cache);
            } else {
                if (key == null) {
                    key = generateKey(context, result);
                }
                doEvict(cache, key);
            }
        }
    }

    protected void doClear(L2Cache cache) {
        try {
            cache.clear();
        } catch (RuntimeException ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    protected void doEvict(L2Cache cache, String key) {
        try {
            cache.delete(key);
        } catch (RuntimeException ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    @Nullable
    private Object findCachedItem(Collection<L2CacheOperationContext> contexts) {
        Object result = NO_RESULT;
        for (L2CacheOperationContext context : contexts) {
            String key = generateKey(context, result);
            Object cached = findInCaches(context, key);
            if (cached != null) {
                return cached;
            } else {
                if (log.isTraceEnabled()) {
                    log.trace("No cache entry for key '" + key + "' in cache(s) " + context.getCacheNames());
                }
            }
        }
        return null;
    }

    @Nullable
    private Object findInCaches(L2CacheOperationContext context, String key) {
        Type type = context.getMethod().getAnnotatedReturnType().getType();
        for (L2Cache cache : context.getCaches()) {
            Object wrapper = cache.get(key, type);
            if (wrapper != null) {
                if (log.isTraceEnabled()) {
                    log.trace("Cache entry for key '" + key + "' found in cache '" + cache.name() + "'");
                }
                return wrapper;
            }
        }
        return null;
    }

    protected Object invokeOperation(L2CacheOperationInvoker invoker) {
        return invoker.invoke();
    }

    @Nullable
    private Object unwrapReturnValue(Object returnValue) {
        return ObjectUtils.unwrapOptional(returnValue);
    }

    @Nullable
    private Object wrapCacheValue(Method method, @Nullable Object cacheValue) {
        if (method.getReturnType() == Optional.class &&
                (cacheValue == null || cacheValue.getClass() != Optional.class)) {
            return Optional.ofNullable(cacheValue);
        }
        return cacheValue;
    }

    private String generateKey(L2CacheOperationContext context, @Nullable Object result) {
        String key = context.generateKey(result);
        if (key == null) {
            throw new IllegalArgumentException("Null key returned for cache operation (maybe you are " +
                    "using named params on classes without debug info?) " + context.metadata.operation);
        }
        if (log.isTraceEnabled()) {
            log.trace("Computed cache key '" + key + "' for operation " + context.metadata.operation);
        }
        return key;
    }

    @Nullable
    public L2CacheOperationSource getCacheOperationSource() {
        return this.l2CacheOperationSource;
    }

    @Override
    public void afterSingletonsInstantiated() {
        this.initialized = true;
    }

    private Class<?> getTargetClass(Object target) {
        return AopProxyUtils.ultimateTargetClass(target);
    }

    protected Collection<? extends L2Cache> getCaches(
            L2CacheOperationInvocationContext<L2CacheOperation> context, L2CacheResolver cacheResolver) {

        Collection<? extends L2Cache> l2Caches = cacheResolver.resolveCaches(context);
        if (l2Caches.isEmpty()) {
            throw new IllegalStateException("No cache could be resolved for '" +
                    context.getOperation() + "' using resolver '" + cacheResolver +
                    "'. At least one cache should be provided per cache operation.");
        }
        return l2Caches;
    }

    protected L2CacheOperationContext getOperationContext(
            L2CacheOperation operation, Method method, Object[] args, Object target, Class<?> targetClass) {

        L2CacheOperationMetadata metadata = getCacheOperationMetadata(operation, method, targetClass);
        return new L2CacheOperationContext(metadata, args, target);
    }

    protected L2CacheOperationMetadata getCacheOperationMetadata(
            L2CacheOperation operation, Method method, Class<?> targetClass) {

        L2CacheOperationCacheKey cacheKey = new L2CacheOperationCacheKey(operation, method, targetClass);
        L2CacheOperationMetadata metadata = this.metadataCache.get(cacheKey);
        if (metadata == null) {
            L2CacheResolver operationCacheResolver = getCacheResolver();
            Assert.state(operationCacheResolver != null, "No CacheResolver/CacheManager set");
            metadata = new L2CacheOperationMetadata(operation, method, targetClass, operationCacheResolver);
            this.metadataCache.put(cacheKey, metadata);
        }
        return metadata;
    }

    private class L2CachePutRequest {

        private final L2CacheOperationContext context;

        private final String key;

        public L2CachePutRequest(L2CacheOperationContext context, String key) {
            this.context = context;
            this.key = key;
        }

        public void apply(@Nullable Object result) {
            if (this.context.canPutToCache(result)) {
                for (L2Cache cache : this.context.getCaches()) {
                    cache.put(this.key, result);
                }
            }
        }
    }

    private class L2CacheOperationContexts {
        private final MultiValueMap<Class<? extends L2CacheOperation>, L2CacheOperationContext> contexts;

        public L2CacheOperationContexts(Collection<? extends L2CacheOperation> operations, Method method,
                                        Object[] args, Object target, Class<?> targetClass) {
            this.contexts = new LinkedMultiValueMap<>(operations.size());
            for (L2CacheOperation op : operations) {
                this.contexts.add(op.getClass(), getOperationContext(op, method, args, target, targetClass));
            }
        }

        public Collection<L2CacheOperationContext> get(Class<? extends L2CacheOperation> operationClass) {
            Collection<L2CacheOperationContext> result = this.contexts.get(operationClass);
            return (result != null ? result : Collections.emptyList());
        }
    }

    protected class L2CacheOperationContext implements L2CacheOperationInvocationContext<L2CacheOperation> {

        private final L2CacheOperationMetadata metadata;

        private final Object[] args;

        private final Object target;

        private final Collection<? extends L2Cache> caches;

        private final Collection<String> cacheNames;

        public L2CacheOperationContext(L2CacheOperationMetadata metadata, Object[] args, Object target) {
            this.metadata = metadata;
            this.args = extractArgs(metadata.method, args);
            this.target = target;
            this.caches = L2CacheAspectSupport.this.getCaches(this, metadata.l2CacheResolver);
            this.cacheNames = createCacheNames(this.caches);
        }

        @Override
        public L2CacheOperation getOperation() {
            return this.metadata.operation;
        }

        @Override
        public Object getTarget() {
            return this.target;
        }

        @Override
        public Method getMethod() {
            return this.metadata.method;
        }

        @Override
        public Object[] getArgs() {
            return this.args;
        }

        private Object[] extractArgs(Method method, Object[] args) {
            if (!method.isVarArgs()) {
                return args;
            }
            Object[] varArgs = ObjectUtils.toObjectArray(args[args.length - 1]);
            Object[] combinedArgs = new Object[args.length - 1 + varArgs.length];
            System.arraycopy(args, 0, combinedArgs, 0, args.length - 1);
            System.arraycopy(varArgs, 0, combinedArgs, args.length - 1, varArgs.length);
            return combinedArgs;
        }

        protected String generateKey(@Nullable Object result) {

            StringBuilder cacheKey = new StringBuilder();
            CacheKeyType cacheKeyType = this.metadata.operation.getCacheKeyType();
            if (CacheKeyType.FIX.equals(cacheKeyType)) {
                cacheKey.append(this.metadata.operation.getCacheKey());
            } else if (CacheKeyType.EXPRESSION.equals(cacheKeyType)) {
                cacheKey.append(getExpressionCacheKey(this.metadata.operation.getCacheKey()));
            } else {
                cacheKey.append(getArgumentsCacheKey());
            }
            return cacheKey.toString();
        }

        /**
         * 表达式key
         *
         * @param expression 表达式
         * @return 表达式缓存key
         */
        private String getExpressionCacheKey(String expression) {
            //noinspection ConstantConditions
            return new SpelExpressionParser().parseExpression(expression)
                    .getValue(new MethodBasedEvaluationContext(null, getMethod(),
                            getArgs(), new DefaultParameterNameDiscoverer())).toString();
        }

        /**
         * 参数key
         *
         * @return 参数Key
         */
        private String getArgumentsCacheKey() {

            StringBuilder builder = new StringBuilder();
            builder.append(getMethod().getName()).append(L2CacheConstant.L2CACHE_KEY_SPLIT);
            Object[] argsArr = getArgs();
            int cnt = argsArr.length;
            for (Object o : argsArr) {
                builder.append(JSONObject.toJSON(o).toString());
                if (--cnt > 0) {
                    builder.append(L2CacheConstant.L2CACHE_KEY_SPLIT);
                }
            }
            return builder.toString();
        }

        protected boolean canPutToCache(@Nullable Object value) {

            Class<? extends L2CacheStrategy> strategy = null;
            if (this.metadata.operation instanceof L2CacheableOperation) {
                strategy = ((L2CacheableOperation) this.metadata.operation).getStrategy();
            } else if (this.metadata.operation instanceof L2CachePutOperation) {
                strategy = ((L2CachePutOperation) this.metadata.operation).getStrategy();
            }

            if (Objects.nonNull(strategy)) {
                L2CacheStrategy cacheStrategy = beanFactory.getBean(strategy);
                return cacheStrategy.enableCache(value);
            }
            return false;
        }

        protected Collection<? extends L2Cache> getCaches() {
            return this.caches;
        }

        protected Collection<String> getCacheNames() {
            return this.cacheNames;
        }

        private Collection<String> createCacheNames(Collection<? extends L2Cache> caches) {
            Collection<String> names = new ArrayList<>();
            for (L2Cache cache : caches) {
                names.add(cache.name());
            }
            return names;
        }

        /**
         * 判断对象或集合是否为空
         *
         * @param value 对象
         * @return true or false
         */
        private boolean empty(Object value) {
            if (Objects.isNull(value)) {
                return true;
            }
            if (value instanceof Collection) {
                //noinspection rawtypes
                Collection collect = (Collection) value;
                return CollectionUtils.isEmpty(collect);
            }
            return false;
        }
    }

    protected static class L2CacheOperationMetadata {

        private final L2CacheOperation operation;

        private final Method method;

        private final Class<?> targetClass;

        private final Method targetMethod;

        private final AnnotatedElementKey methodKey;

        private final L2CacheResolver l2CacheResolver;

        public L2CacheOperationMetadata(L2CacheOperation operation, Method method, Class<?> targetClass,
                                        L2CacheResolver cacheResolver) {

            this.operation = operation;
            this.method = BridgeMethodResolver.findBridgedMethod(method);
            this.targetClass = targetClass;
            this.targetMethod = (!Proxy.isProxyClass(targetClass) ?
                    AopUtils.getMostSpecificMethod(method, targetClass) : this.method);
            this.methodKey = new AnnotatedElementKey(this.targetMethod, targetClass);
            this.l2CacheResolver = cacheResolver;
        }
    }

    private static final class L2CacheOperationCacheKey implements Comparable<L2CacheOperationCacheKey> {

        private final L2CacheOperation cacheOperation;

        private final AnnotatedElementKey methodCacheKey;

        private L2CacheOperationCacheKey(L2CacheOperation cacheOperation, Method method, Class<?> targetClass) {
            this.cacheOperation = cacheOperation;
            this.methodCacheKey = new AnnotatedElementKey(method, targetClass);
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof L2CacheOperationCacheKey)) {
                return false;
            }
            L2CacheOperationCacheKey otherKey = (L2CacheOperationCacheKey) other;
            return (this.cacheOperation.equals(otherKey.cacheOperation) &&
                    this.methodCacheKey.equals(otherKey.methodCacheKey));
        }

        @Override
        public int hashCode() {
            return (this.cacheOperation.hashCode() * 31 + this.methodCacheKey.hashCode());
        }

        @Override
        public String toString() {
            return this.cacheOperation + " on " + this.methodCacheKey;
        }

        @Override
        public int compareTo(L2CacheOperationCacheKey other) {
            int result = this.cacheOperation.getName().compareTo(other.cacheOperation.getName());
            if (result == 0) {
                result = this.methodCacheKey.compareTo(other.methodCacheKey);
            }
            return result;
        }
    }
}
