/*
 *Copyright [2024] [The Original Author]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package flipkart.tef.bizlogics;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.internal.BytecodeGen;
import com.google.inject.matcher.Matchers;
import flipkart.tef.annotations.EmitData;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DataAdapterBizlogicTest {

    @EmitData(name = "testData")
    static class TestDataAdapterBizlogic extends DataAdapterBizlogic<Object> {
        @Override
        public Object adapt(TefContext tefContext) {
            return null;
        }
    }

    static class TestDataAdapterBizlogic1 extends DataAdapterBizlogic<Object> {
        @Override
        public Object adapt(TefContext tefContext) {
            return null;
        }
    }

    @Test
    public void testGetEmittedDataName() throws Exception {
        //setup
        Class<? extends DataAdapterBizlogic> clazz = TestDataAdapterBizlogic.class;
        ConcurrentHashMap<String, String> map = getEmitDataCache(clazz.getSuperclass());

        //test
        String emittedDataName = DataAdapterBizlogic.getEmittedDataName(clazz);

        //validate
        assertEquals("testData", emittedDataName);
        assertEquals("testData", map.get(clazz.getName()));
    }

    @Test
    public void testGetEmittedDataNameForAnnotationAbsence() throws Exception {
        //setup
        Class<? extends DataAdapterBizlogic> clazz = TestDataAdapterBizlogic1.class;
        ConcurrentHashMap<String, String> map = getEmitDataCache(clazz.getSuperclass());


        //test
        String emittedDataName = DataAdapterBizlogic.getEmittedDataName(clazz);

        //validate
        assertEquals("", emittedDataName);
        assertEquals("", map.get(clazz.getName()));
    }

    @Test
    public void testGetEmittedDataNameWithGuiceProxy() throws Exception {
        // setup
        Injector injector = Guice.createInjector(new GuiceModule());
        TestDataAdapterBizlogic dataAdapterBizlogic = injector.getInstance(TestDataAdapterBizlogic.class);
        ConcurrentHashMap<String, String> map = getEmitDataCache(dataAdapterBizlogic.getClass().getSuperclass().getSuperclass());

        // test
        String emittedDataName = DataAdapterBizlogic.getEmittedDataName(dataAdapterBizlogic.getClass());

        // validate
        assertTrue(dataAdapterBizlogic.getClass().getName().contains(BytecodeGen.ENHANCER_BY_GUICE_MARKER));
        assertEquals("testData", emittedDataName);
        assertEquals("testData", map.get(dataAdapterBizlogic.getClass().getName()));
    }

    private ConcurrentHashMap<String, String> getEmitDataCache(Class<?> clazz) throws Exception {
        Field emitDataField = clazz.getDeclaredField("emitDataCache");
        emitDataField.setAccessible(true);
        ConcurrentHashMap<String, String> map = (ConcurrentHashMap<String, String>) emitDataField.get(null);
        map.clear();
        return map;
    }

    class GuiceModule extends AbstractModule {
        @Override
        protected void configure() {
            bindInterceptor(
                    Matchers.subclassesOf(TestDataAdapterBizlogic.class),
                    Matchers.any(),
                    new CustomInterceptor()
            );
        }
    }

    public class CustomInterceptor implements MethodInterceptor {
        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            // Proceed with the actual method invocation
            return invocation.proceed();
        }
    }
}