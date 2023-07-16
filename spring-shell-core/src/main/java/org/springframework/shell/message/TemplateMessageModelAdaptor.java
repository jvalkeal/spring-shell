/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.shell.message;

import org.stringtemplate.v4.Interpreter;
import org.stringtemplate.v4.ModelAdaptor;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.misc.STNoSuchPropertyException;

public class TemplateMessageModelAdaptor implements ModelAdaptor<TemplateMessage> {

	@Override
	public Object getProperty(Interpreter interp, ST self, TemplateMessage model, Object property, String propertyName)
			throws STNoSuchPropertyException {
		ShellMessages messages = model.messages();
		String resolve = messages.resolve(model.context(), propertyName, null, "");
		return resolve;
	}

}
