/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ratpackframework.handling;

import com.google.common.collect.ImmutableList;
import org.ratpackframework.handling.internal.ServiceExtractor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * Convenience handler super class that provides syntactic sugar for accessing contextual objects.
 * <p>
 * Subclasses must implement exactly one method named {@code "handle"} that accepts a {@link Context} as the first parameter,
 * and at least one other parameter of any type.
 * <p>
 * Each parameter after the first {@link Context} parameter is expected to be a contextual object.
 * It's value will be the result of calling {@link Context#get(Class)} with the parameter type.
 * <p>
 * The following two handlers are functionally equivalent:
 * <pre class="tested">
 * import org.ratpackframework.handling.*;
 * import org.ratpackframework.file.FileSystemBinding;
 *
 * public class VerboseHandler implements Handler {
 *   public void handle(Context context) {
 *     FileSystemBinding fileSystemBinding = context.get(FileSystemBinding.class);
 *     context.getResponse().send(fileSystemBinding.getFile().getAbsolutePath());
 *   }
 * }
 *
 * public class SuccinctHandler extends ServiceUsingHandler {
 *   public void handle(Context context, FileSystemBinding fileSystemBinding) {
 *     context.getResponse().send(fileSystemBinding.getFile().getAbsolutePath());
 *   }
 * }
 * </pre>
 * <p>
 * If the parameters cannot be satisifed, a {@link org.ratpackframework.registry.NotInRegistryException} will be thrown.
 * <p>
 * If there is no suitable {@code handle(Context, ...)} method, a {@link NoSuitableHandleMethodException} will be thrown at construction time.
 *
 */
public abstract class ServiceUsingHandler implements Handler {

  private final List<Class<?>> serviceTypes;
  private final Method handleMethod;

  /**
   * Constructor.
   *
   * @throws NoSuitableHandleMethodException if this class doesn't provide a suitable handle method.
   */
  protected ServiceUsingHandler() throws NoSuitableHandleMethodException {
    Class<?> thisClass = this.getClass();

    Method handleMethod = null;
    for (Method method : thisClass.getDeclaredMethods()) {
      if (!method.getName().equals("handle")) {
        continue;
      }

      Class<?>[] parameterTypes = method.getParameterTypes();
      if (parameterTypes.length < 2) {
        continue;
      }

      if (!parameterTypes[0].equals(Context.class)) {
        continue;
      }

      handleMethod = method;
      break;
    }

    if (handleMethod == null) {
      throw new NoSuitableHandleMethodException(thisClass);
    }

    this.handleMethod = handleMethod;
    Class<?>[] parameterTypes = handleMethod.getParameterTypes();
    this.serviceTypes = ImmutableList.copyOf(Arrays.asList(parameterTypes).subList(1, parameterTypes.length));
  }

  /**
   * Invokes the custom "handle" method, extracting necessary parameters from the context to satisfy the call.
   *
   * @param context The context to handle
   */
  public final void handle(Context context) {
    Object[] args = new Object[serviceTypes.size() + 1];
    args[0] = context;
    ServiceExtractor.extract(serviceTypes, context, args, 1);
    try {
      handleMethod.invoke(this, args);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      Throwable root = e.getTargetException();
      if (root instanceof RuntimeException) {
        throw (RuntimeException) root;
      } else {
        throw new RuntimeException(root);
      }
    }
  }

  /**
   * Exception thrown if the subclass doesn't provide a valid handle method.
   */
  public static class NoSuitableHandleMethodException extends RuntimeException {
    private static final long serialVersionUID = 0;
    private NoSuitableHandleMethodException(Class<?> clazz) {
      super("No injectable handle method found for " + clazz.getName());
    }
  }

}
