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
package org.springframework.shell.command.parser;

import java.text.MessageFormat;

/**
 * Contains all the messages that can be produced during parsing. Each message
 * has a kind (WARNING, ERROR) and a code number.
 *
 * @author Janne Valkealahti
 */
public enum ParserMessage {

	ILLEGAL_CONTENT_BEFORE_COMMANDS(Type.ERROR, 1000, "Illegal content before commands ''{0}''"),
	MANDATORY_OPTION_MISSING(Type.ERROR, 2000, "Missing option, longnames=''{0}'', shortnames=''{1}''"),
	UNRECOGNISED_OPTION(Type.ERROR, 2001, "Unrecognised option ''{0}''")
	;

	private Type type;
	private int code;
	private String message;

	ParserMessage(Type type, int code, String message) {
		this.type = type;
		this.code = code;
		this.message = message;
	}

	public int getCode() {
		return code;
	}

	public Type getType() {
		return type;
	}

	public String formatMessage(int position, Object... inserts) {
		StringBuilder msg = new StringBuilder();
		msg.append(code);
		switch (type) {
			case WARNING:
				msg.append("W");
				break;
			case ERROR:
				msg.append("E");
				break;
		}
		msg.append(":");
		if (position != -1) {
			msg.append("(pos ").append(position).append("): ");
		}
		msg.append(MessageFormat.format(message, inserts));
		return msg.toString();
	}

	public enum Type {
		WARNING,
		ERROR
	}
}
