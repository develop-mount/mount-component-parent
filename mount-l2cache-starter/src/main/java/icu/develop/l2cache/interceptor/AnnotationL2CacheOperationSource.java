package icu.develop.l2cache.interceptor;

import icu.develop.l2cache.annotation.DefaultL2CacheAnnotationParser;
import icu.develop.l2cache.annotation.L2CacheAnnotationParser;
import org.springframework.cache.annotation.CacheAnnotationParser;
import org.springframework.cache.interceptor.CacheOperation;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Description:
 *
 * @author linfeng
 * @version 1.0.0
 * @since 2023/8/12 21:22
 */
public class AnnotationL2CacheOperationSource extends AbstractL2CacheOperationSource implements Serializable {

    private final boolean publicMethodsOnly;

    private final Set<L2CacheAnnotationParser> annotationParsers;


    /**
     * Create a default AnnotationCacheOperationSource, supporting public methods
     * that carry the {@code Cacheable} and {@code CacheEvict} annotations.
     */
    public AnnotationL2CacheOperationSource() {
        this(true);
    }

    /**
     * Create a default {@code AnnotationCacheOperationSource}, supporting public methods
     * that carry the {@code Cacheable} and {@code CacheEvict} annotations.
     *
     * @param publicMethodsOnly whether to support only annotated public methods
     *                          typically for use with proxy-based AOP), or protected/private methods as well
     *                          (typically used with AspectJ class weaving)
     */
    public AnnotationL2CacheOperationSource(boolean publicMethodsOnly) {
        this.publicMethodsOnly = publicMethodsOnly;
        this.annotationParsers = Collections.singleton(new DefaultL2CacheAnnotationParser());
    }

    /**
     * Create a custom AnnotationCacheOperationSource.
     *
     * @param annotationParser the CacheAnnotationParser to use
     */
    public AnnotationL2CacheOperationSource(L2CacheAnnotationParser annotationParser) {
        this.publicMethodsOnly = true;
        Assert.notNull(annotationParser, "CacheAnnotationParser must not be null");
        this.annotationParsers = Collections.singleton(annotationParser);
    }

    /**
     * Create a custom AnnotationCacheOperationSource.
     *
     * @param annotationParsers the CacheAnnotationParser to use
     */
    public AnnotationL2CacheOperationSource(L2CacheAnnotationParser... annotationParsers) {
        this.publicMethodsOnly = true;
        Assert.notEmpty(annotationParsers, "At least one CacheAnnotationParser needs to be specified");
        this.annotationParsers = new LinkedHashSet<>(Arrays.asList(annotationParsers));
    }

    /**
     * Create a custom AnnotationCacheOperationSource.
     *
     * @param annotationParsers the CacheAnnotationParser to use
     */
    public AnnotationL2CacheOperationSource(Set<L2CacheAnnotationParser> annotationParsers) {
        this.publicMethodsOnly = true;
        Assert.notEmpty(annotationParsers, "At least one CacheAnnotationParser needs to be specified");
        this.annotationParsers = annotationParsers;
    }

    @Override
    public boolean isCandidateClass(Class<?> targetClass) {
        for (L2CacheAnnotationParser parser : this.annotationParsers) {
            if (parser.isCandidateClass(targetClass)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected Collection<L2CacheOperation> findCacheOperations(Class<?> clazz) {
        return determineCacheOperations(parser -> parser.parseCacheAnnotations(clazz));
    }

    @Override
    protected Collection<L2CacheOperation> findCacheOperations(Method method) {
        return determineCacheOperations(parser -> parser.parseCacheAnnotations(method));
    }


    /**
     * Determine the cache operation(s) for the given {@link L2CacheOperationProvider}.
     * <p>This implementation delegates to configured
     * {@link CacheAnnotationParser CacheAnnotationParsers}
     * for parsing known annotations into Spring's metadata attribute class.
     * <p>Can be overridden to support custom annotations that carry caching metadata.
     *
     * @param provider the cache operation provider to use
     * @return the configured caching operations, or {@code null} if none found
     */
    @Nullable
    protected Collection<L2CacheOperation> determineCacheOperations(L2CacheOperationProvider provider) {
        Collection<L2CacheOperation> ops = null;
        for (L2CacheAnnotationParser parser : this.annotationParsers) {
            Collection<L2CacheOperation> annOps = provider.getCacheOperations(parser);
            if (annOps != null) {
                if (ops == null) {
                    ops = annOps;
                } else {
                    Collection<L2CacheOperation> combined = new ArrayList<>(ops.size() + annOps.size());
                    combined.addAll(ops);
                    combined.addAll(annOps);
                    ops = combined;
                }
            }
        }
        return ops;
    }


    /**
     * By default, only public methods can be made cacheable.
     */
    @Override
    protected boolean allowPublicMethodsOnly() {
        return this.publicMethodsOnly;
    }


    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AnnotationL2CacheOperationSource)) {
            return false;
        }
        AnnotationL2CacheOperationSource otherCos = (AnnotationL2CacheOperationSource) other;
        return (this.annotationParsers.equals(otherCos.annotationParsers) &&
                this.publicMethodsOnly == otherCos.publicMethodsOnly);
    }

    @Override
    public int hashCode() {
        return this.annotationParsers.hashCode();
    }


    /**
     * Callback interface providing {@link CacheOperation} instance(s) based on
     * a given {@link CacheAnnotationParser}.
     */
    @FunctionalInterface
    protected interface L2CacheOperationProvider {

        /**
         * Return the {@link CacheOperation} instance(s) provided by the specified parser.
         *
         * @param parser the parser to use
         * @return the cache operations, or {@code null} if none found
         */
        @Nullable
        Collection<L2CacheOperation> getCacheOperations(L2CacheAnnotationParser parser);
    }
}
