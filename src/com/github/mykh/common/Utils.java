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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

import android.util.Log;

public class Utils {
	public static final String LOGGER_TAG = "com.github.mykh.SysInfo";
	public static final boolean SHOW_UNIMPLEMENTED_ITEMS = false;
	public static final int KB = 1024;
	public static final int MB = 1024 * 1024;
	public static final Locale locale = Locale.getDefault();
	
	public static String dumpObject(Object o) {
		StringBuilder buffer = new StringBuilder();
		Class<?> oClass = o.getClass();
		if (oClass.isArray()) {
			buffer.append("Array: ");
			buffer.append("[");
			for (int i = 0; i < Array.getLength(o); i++) {
				Object value = Array.get(o, i);
				if (value.getClass().isPrimitive()) {
					buffer.append(value);
					if (i != (Array.getLength(o) - 1))
						buffer.append(",");
				} else {
					buffer.append(dumpObject(value));
				}
			}
			buffer.append("]\n");
		} else {
			buffer.append("Class: " + oClass.getName());
			buffer.append("{\n");
			while (oClass != null) {
				Field[] fields = oClass.getDeclaredFields();
				for (int i = 0; i < fields.length; i++) {
					fields[i].setAccessible(true);
					buffer.append(fields[i].getName());
					buffer.append("=");
					try {
						Object value = fields[i].get(o);
						if (value != null) {
							if (value.getClass().isPrimitive()) {
								buffer.append(value);
							} else {
								buffer.append(dumpObject(value));
							}
						}
					} catch (IllegalAccessException e) {
						buffer.append(e.getMessage());
					}
					buffer.append("\n");
				}
				oClass = oClass.getSuperclass();
			}
			buffer.append("}\n");
		}
		return buffer.toString();
	}
	
	private String getCatResult(String filePath) {
		ProcessBuilder cmd;
		StringBuilder result = new StringBuilder();
		try {
			String[] args = { "/system/bin/cat", filePath };
			cmd = new ProcessBuilder(args);

			Process process = cmd.start();
			InputStream in = process.getInputStream();

			int bytesRead = 0;
			final int bytesToRead = 1024;
			byte[] buff = new byte[bytesToRead];
			while ((bytesRead = in.read(buff)) != -1) {
				result.append(new String(buff, 0, bytesRead));
			}
			in.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return result.toString();
	}

	public static int getClassFieldInt(String className, String fieldName, int defaultValue) {
		try {
			Field field = Class.forName(className).getField(fieldName);
			int val = field.getInt(field);
			return val;
		} catch (Exception e) {
			Log.e(LOGGER_TAG, e.getMessage());
			return defaultValue;
		}
	}
	
	public static String getClassFieldStrOrNull(Class<?> _class, String fieldName) {
		try {
			Field filed = _class.getField(fieldName);
			String str = (String) filed.get(filed);
			return str;
		} catch (Exception e) {
			Log.e(LOGGER_TAG, e.getMessage());
			return null;
		}
	}

	public static String getClassFieldStrOrNull(String className, String fieldName) {
		try
		{
		  Class<?> _class = Class.forName(className);
		  return getClassFieldStrOrNull(_class, fieldName);
	    } catch (Exception e) {
	    	Log.e(LOGGER_TAG, e.getMessage());
	    	return null;
	    }
	}
	
	public static Object invokeObjectMethodObjectOrNull(Object obj, String methodName) {
		try {
			Method method = obj.getClass().getMethod(methodName, (Class[]) null);
			Object res = method.invoke(obj, (Object[]) null);
			return res;
		} catch (Exception e) {
			Log.e(LOGGER_TAG, e.getMessage());
			return null;
		}
	}

	public static Object invokeClassMethodObjectOrNull(String className, String methodName) {
		try {
			return invokeClassMethodObjectOrNull(Class.forName(className), methodName);
		} catch (ClassNotFoundException e) {
			Log.e(LOGGER_TAG, e.getMessage());
			return null;
		}
	}
	
	public static Object invokeClassMethodObjectOrNull(Class<?> _class, String methodName) {
		try {
			Method method = _class.getMethod(methodName, (Class[]) null);
			Object res = method.invoke(_class, (Object[]) null);
			return res;
		} catch (Exception e) {
			Log.e(LOGGER_TAG, e.getMessage());
			return null;
		}
	}

	public static String invokeClassMethodStrOrNull(String className, String methodName) {
		return (String) invokeClassMethodObjectOrNull(className, methodName);
	}

	public static Boolean invokeClassMethodBoolOrNull(String className, String methodName) {
		return (Boolean) invokeClassMethodObjectOrNull(className, methodName);
	}
	
	public static String readFileAsString(String filePath) throws java.io.IOException {
		StringBuilder fileData = new StringBuilder();
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		try {
			char[] buf = new char[1 * KB];
			int numRead = 0;
			while ((numRead = reader.read(buf)) != -1) {
				fileData.append(buf, 0, numRead);
			}
		} finally {
			reader.close();
		}
		return fileData.toString();
	}
	
	public static void parseConfString(String content, List<String> names, List<String> values) {
		final String valueDelimiter = ":";
		names.clear();
		values.clear();
		String[] lines = content.split("[\\r?\\n]+");
		for (String line : lines) {
			String[] param = line.split(valueDelimiter, 2);
			if (param.length == 2) {
				names.add(param[0].trim());
				values.add(param[1].trim());
			} else {
				names.add(line);
				values.add("");
			}
		}
	}
}
