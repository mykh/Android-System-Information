/*
* Android System Information
* Copyright (C) 2010-2012 mykh
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.github.mykh.views;

import com.github.mykh.common.Config;
import com.github.mykh.common.ConfigBase;
import com.github.mykh.common.ConfigList;
import com.github.mykh.common.Utils;

public class ConfigFormatter_PlainText implements IConfigFormatter {
	private void appendSpaces(StringBuilder sb, int level) {
		for (int i = 0; i < level; i++) {
			sb.append(' ');
		}
	}

	public void process(Config node, StringBuilder sb, int level) {
		if (!Utils.SHOW_UNIMPLEMENTED_ITEMS && node.getName().startsWith("*")) {
			return;
		}
		appendSpaces(sb, level);
		sb.append(node.getName());
		sb.append(": ");
		if (node.getValue() == null) {
			sb.append("<None>");
		} else {
			sb.append(node.getValue());
		}
		sb.append("\n");
	}

	public void process(ConfigList list, StringBuilder sb, int level) {
		if ((list.getName() != null) && (list.getName().length() > 0)) {
			appendSpaces(sb, level);
			sb.append("# ");
			sb.append(list.getName());
			sb.append(" #");
			sb.append("\n");
		}
		for (ConfigBase node : list.getItems()) {
			if (node instanceof Config) {
				process((Config) node, sb, level);
			} else if (node instanceof ConfigList) {
				if (sb.length() != 0) {
					sb.append("\n");
				}
				process((ConfigList) node, sb, level + 1);
			}
		}
	}
}
