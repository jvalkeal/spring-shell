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
package org.springframework.shell.component.view;

import java.util.ArrayList;
import java.util.EnumSet;

public record KeyEvent(String data, KeyType key, EnumSet<ModType> mod) {

    public static KeyEvent ofCharacter(String data) {
        return new KeyEvent(data, null, EnumSet.noneOf(ModType.class));
    }

    public static KeyEvent ofCharacter(String data, EnumSet<ModType> mod) {
        return new KeyEvent(data, null, mod);
    }

    public static KeyEvent ofType(KeyType type) {
        return new KeyEvent(null, type, EnumSet.noneOf(ModType.class));
    }

    public static KeyEvent ofType(KeyType type, EnumSet<ModType> mod) {
        return new KeyEvent(null, type, mod);
    }

    public enum ModType {
        CTRL,
        ALT;

        public static EnumSet<ModType> of(boolean ctrl, boolean alt) {
            if (!ctrl && !alt) {
                return EnumSet.noneOf(ModType.class);
            }
            ArrayList<ModType> list = new ArrayList<ModType>();
            if (ctrl) {
                list.add(CTRL);
            }
            if (alt) {
                list.add(ALT);
            }
            return EnumSet.copyOf(list);
        }
    }

    public enum KeyType {
        DOWNARROW("DownArrow"),
        UPARROW("UpArrow"),
        LEFTARROW("LeftArrow"),
        RIGHTARROW("RightArrow");

        private final String name;

        KeyType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
