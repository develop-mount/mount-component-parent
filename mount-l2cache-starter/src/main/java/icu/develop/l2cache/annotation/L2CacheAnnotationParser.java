package icu.develop.l2cache.annotation;

import icu.develop.l2cache.interceptor.L2CacheOperation;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Description:
 *
 * @author linfeng
 * @version 1.0.0
 * @since 2023/8/12 21:27
 */
public interface L2CacheAnnotationParser {

    /**
     * Determine whether the given class is a candidate for cache operations
     * in the annotation format of this {@code CacheAnnotationParser}.
     * <p>If this method returns {@code false}, the methods on the given class
     * will not get traversed for {@code #parseCacheAnnotations} introspection.
     * Returning {@code false} is therefore an optimization for non-affected
     * classes, whereas {@code true} simply means that the class needs to get
     * fully introspected for each method on the given class individually.
     *
     * @param targetClass the class to introspect
     * @return {@code false} if the class is known to have no cache operation
     * annotations at class or method level; {@code true} otherwise. The default
     * implementation returns {@code true}, leading to regular introspection.
     * @since 5.2
     */
    default boolean isCandidateClass(Class<?> targetClass) {
        return true;
    }

    /**
     * Parse the cache definition for the given class,
     * based on an annotation type understood by this parser.
     * <p>This essentially parses a known cache annotation into Spring's metadata
     * attribute class. Returns {@code null} if the class is not cacheable.
     *
     * @param type the annotated class
     * @return the configured caching operation, or {@code null} if none found
     */
    @Nullable
    Collection<L2CacheOperation> parseCacheAnnotations(Class<?> type);

    /**
     * Parse the cache definition for the given method,
     * based on an annotation type understood by this parser.
     * <p>This essentially parses a known cache annotation into Spring's metadata
     * attribute class. Returns {@code null} if the method is not cacheable.
     *
     * @param method the annotated method
     * @return the configured caching operation, or {@code null} if none found
     */
    @Nullable
    Collection<L2CacheOperation> parseCacheAnnotations(Method method);
}
