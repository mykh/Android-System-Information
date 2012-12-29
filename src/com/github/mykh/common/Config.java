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

package com.github.mykh.common;

public class Config extends ConfigBase {
	protected String value;

	public Config(String Name, String Value, String Hint) {
		super(Name, Hint);
		this.value = Value;
	}

	public Config(String Name, String Value) {
		this(Name, Value, "");
	}

	public String getValue() {
		return value;
	}
};
