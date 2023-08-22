package icu.develop.l2cache.annotation;

import icu.develop.l2cache.interceptor.L2CacheEvictOperation;
import icu.develop.l2cache.interceptor.L2CacheOperation;
import icu.develop.l2cache.interceptor.L2CachePutOperation;
import icu.develop.l2cache.interceptor.L2CacheableOperation;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Description:
 *
 * @author linfeng
 * @version 1.0.0
 * @since 2023/8/12 21:29
 */
public class DefaultL2CacheAnnotationParser implements L2CacheAnnotationParser, Serializable {

    private static final Set<Class<? extends Annotation>> CACHE_OPERATION_ANNOTATIONS = new LinkedHashSet<>(8);

    static {
        CACHE_OPERATION_ANNOTATIONS.add(L2Cacheable.class);
        CACHE_OPERATION_ANNOTATIONS.add(L2CacheEvict.class);
        CACHE_OPERATION_ANNOTATIONS.add(L2CachePut.class);
        CACHE_OPERATION_ANNOTATIONS.add(L2Caching.class);
    }

    @Override
    public boolean isCandidateClass(Class<?> targetClass) {
        return AnnotationUtils.isCandidateClass(targetClass, CACHE_OPERATION_ANNOTATIONS);
    }

    @Override
    public Collection<L2CacheOperation> parseCacheAnnotations(Class<?> type) {
        return commonParseCacheAnnotations(type);
    }

    @Override
    public Collection<L2CacheOperation> parseCacheAnnotations(Method method) {
        return commonParseCacheAnnotations(method);
    }

    @Nullable
    private Collection<L2CacheOperation> commonParseCacheAnnotations(AnnotatedElement ae) {
        Collection<L2CacheOperation> ops = parseCacheAnnotations(ae, false);
        if (ops != null && ops.size() > 1) {
            // More than one operation found -> local declarations override interface-declared ones...
            Collection<L2CacheOperation> localOps = parseCacheAnnotations(ae, true);
            if (localOps != null) {
                return localOps;
            }
        }
        return ops;
    }


    @Nullable
    private Collection<L2CacheOperation> parseCacheAnnotations(
            AnnotatedElement ae, boolean localOnly) {

        Collection<? extends Annotation> anns = (localOnly ?
                AnnotatedElementUtils.getAllMergedAnnotations(ae, CACHE_OPERATION_ANNOTATIONS) :
                AnnotatedElementUtils.findAllMergedAnnotations(ae, CACHE_OPERATION_ANNOTATIONS));
        if (anns.isEmpty()) {
            return null;
        }

        final Collection<L2CacheOperation> ops = new ArrayList<>(1);
        anns.stream().filter(ann -> ann instanceof L2Cacheable).forEach(
                ann -> ops.add(parseCacheableAnnotation(ae, (L2Cacheable) ann)));
        anns.stream().filter(ann -> ann instanceof L2CacheEvict).forEach(
                ann -> ops.add(parseEvictAnnotation(ae, (L2CacheEvict) ann)));
        anns.stream().filter(ann -> ann instanceof L2CachePut).forEach(
                ann -> ops.add(parsePutAnnotation(ae, (L2CachePut) ann)));
        anns.stream().filter(ann -> ann instanceof L2Caching).forEach(
                ann -> parseCachingAnnotation(ae, (L2Caching) ann, ops));
        return ops;
    }

    private L2CacheableOperation parseCacheableAnnotation(
            AnnotatedElement ae, L2Cacheable cacheable) {

        L2CacheableOperation.Builder builder = new L2CacheableOperation.Builder();

        builder.setName(ae.toString());
        builder.setCacheNames(cacheable.cacheNames());
        builder.setCacheKeyType(cacheable.cacheKeyType());
        builder.setCacheKey(cacheable.cacheKey());
        builder.setStrategy(cacheable.strategy());

        return builder.build();
    }

    private L2CacheEvictOperation parseEvictAnnotation(
            AnnotatedElement ae, L2CacheEvict cacheEvict) {

        L2CacheEvictOperation.Builder builder = new L2CacheEvictOperation.Builder();

        builder.setName(ae.toString());
        builder.setCacheNames(cacheEvict.cacheNames());
        builder.setCacheKey(cacheEvict.cacheKey());
        builder.setCacheKeyType(cacheEvict.cacheKeyType());
        builder.setCacheWide(cacheEvict.allEntries());
        builder.setBeforeInvocation(cacheEvict.beforeInvocation());

        return builder.build();
    }

    private L2CachePutOperation parsePutAnnotation(
            AnnotatedElement ae, L2CachePut cachePut) {

        L2CachePutOperation.Builder builder = new L2CachePutOperation.Builder();

        builder.setName(ae.toString());
        builder.setCacheNames(cachePut.cacheNames());
        builder.setCacheKeyType(cachePut.cacheKeyType());
        builder.setCacheKey(cachePut.cacheKey());
        builder.setStrategy(cachePut.strategy());

        return builder.build();
    }

    private void parseCachingAnnotation(
            AnnotatedElement ae, L2Caching caching, Collection<L2CacheOperation> ops) {

        L2Cacheable[] cacheables = caching.cacheable();
        for (L2Cacheable cacheable : cacheables) {
            ops.add(parseCacheableAnnotation(ae, cacheable));
        }
        L2CacheEvict[] cacheEvicts = caching.evict();
        for (L2CacheEvict cacheEvict : cacheEvicts) {
            ops.add(parseEvictAnnotation(ae, cacheEvict));
        }
        L2CachePut[] cachePuts = caching.put();
        for (L2CachePut cachePut : cachePuts) {
            ops.add(parsePutAnnotation(ae, cachePut));
        }
    }

}
