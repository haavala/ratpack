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

package org.ratpackframework.test.handling;

import org.ratpackframework.api.Nullable;
import org.ratpackframework.http.Headers;
import org.ratpackframework.http.Status;

import java.io.File;

/**
 * Represents the result of invoking a handler.
 */
public interface Invocation {

  @Nullable
  Exception getException();

  Headers getHeaders();

  @Nullable
  String getBodyText();

  @Nullable
  byte[] getBodyBytes();

  Status getStatus();

  boolean isCalledNext();

  boolean isSentResponse(); // This is not named right, as it doesn't include sending files

  @Nullable
  File getSentFile();

  @Nullable
  <T> T rendered(Class<T> type);

}
