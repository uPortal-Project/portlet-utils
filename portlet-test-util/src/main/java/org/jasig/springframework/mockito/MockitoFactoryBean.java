package org.jasig.springframework.mockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * Used to create a spring bean via Mockito
 * 
 * @author Eric Dalquist
 * @param <T> Type of the mock instance to return
 */
public class MockitoFactoryBean<T> extends AbstractFactoryBean<T> {
    private static final Set<Object> MOCK_CACHE = Collections.newSetFromMap(new ConcurrentHashMap<Object, Boolean>());
    
    /**
     * Reset all mocks that have been created with this factory, usually called from the @Before
     * method in a junit test case to reset mocks before each test
     */
    public static void resetAllMocks() {
        reset(MOCK_CACHE.toArray());
    }
    
    private final Class<? extends T> type;

    public MockitoFactoryBean(Class<? extends T> type) {
        this.type = type;
    }

    @Override
    public Class<? extends T> getObjectType() {
        return this.type;
    }

    @Override
    protected T createInstance() throws Exception {
        final T mock = mock(this.type);
        MOCK_CACHE.add(mock);
        return mock;
    }

    @Override
    protected void destroyInstance(T instance) throws Exception {
        MOCK_CACHE.remove(instance);
    }
}