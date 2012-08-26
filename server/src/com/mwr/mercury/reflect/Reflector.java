// License: Refer to the README in the root directory

package com.mwr.mercury.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Reflector
{

	public Class<?> resolve(String s) throws ClassNotFoundException
	{
		return Class.forName(s);
	}

	public Object getProperty(Object obj, String name) throws Exception
	{
		if(obj instanceof Class) {
			// static field
			return ((Class<?>) obj).getField(name).get(null);
		} else {
			Field field = obj.getClass().getField(name);
			return field.get(obj);
		}
	}

	public boolean isPropertyPrimitive(Object obj, String name) throws SecurityException, NoSuchFieldException
	{
		if(obj instanceof Class) {
			// static field
			return ((Class<?>) obj).getField(name).getType().isPrimitive();
		} else {
			Field field = obj.getClass().getField(name);
			return field.getType().isPrimitive();
		}
	}

	public Object createPrimitiveArray(String[] valuesArray, String type)
	{
		// byte, short, int, long, float, double, boolean, char
		int len = valuesArray.length;
		if(type.equals("byte")) {
			byte[] array = new byte[len];
			int i = 0;
			for(String value : valuesArray)
				array[i++] = Byte.parseByte(value);
			return array;
		} else if(type.equals("short")) {
			short[] array = new short[len];
			int i = 0;
			for(String value : valuesArray)
				array[i++] = Short.parseShort(value);
			return array;
		} else if(type.equals("int")) {
			int[] array = new int[len];
			int i = 0;
			for(String value : valuesArray)
				array[i++] = Integer.parseInt(value);
			return array;
		} else if(type.equals("long")) {
			long[] array = new long[len];
			int i = 0;
			for(String value : valuesArray)
				array[i++] = Long.parseLong(value);
			return array;
		} else if(type.equals("float")) {
			float[] array = new float[len];
			int i = 0;
			for(String value : valuesArray)
				array[i++] = Float.parseFloat(value);
			return array;
		} else if(type.equals("double")) {
			double[] array = new double[len];
			int i = 0;
			for(String value : valuesArray)
				array[i++] = Double.parseDouble(value);
			return array;
		} else if(type.equals("boolean")) {
			boolean[] array = new boolean[len];
			int i = 0;
			for(String value : valuesArray)
				array[i++] = Boolean.parseBoolean(value);
			return array;
		} else if(type.equals("char")) {
			char[] array = new char[len];
			int i = 0;
			for(String value : valuesArray)
				array[i++] = Character.valueOf(value.charAt(0));
			return array;
		}
		return null;
	}

	private Constructor<?> getConstructor(Class<?> obj, Object[] a) throws Exception {
		int argc = a.length;
		Constructor<?>[] cons = obj.getConstructors();
		for (Constructor<?> con: cons) {
			Class<?>[] paramClasses = con.getParameterTypes();
			if (paramClasses.length == argc) {
				boolean correct = true;
				for (int i = 0; i < argc; i++) {
					correct = correct & isCompatible(a[i], paramClasses[i]);
				}
				if (correct) {
					return con;
				}
			}
		}
		throw new NoSuchMethodException();
	}

	private Class<?>[] getParameterType(Object[] arguments) throws Exception {
        Class<?>[] ret = new Class[arguments.length];
        int i = 0;
        // byte, short, int, long, float, double, boolean, char
        for(Object arg : arguments) {
            if (arg == null) throw new Exception("Cannot return a parameter type for null.");
            else if(arg instanceof Integer) ret[i++] = Integer.TYPE;
            else if(arg instanceof Short) ret[i++] = Short.TYPE;
            else if(arg instanceof Byte) ret[i++] = Byte.TYPE;
            else if(arg instanceof Long) ret[i++] = Long.TYPE;
            else if(arg instanceof Float) ret[i++] = Float.TYPE;
            else if(arg instanceof Double) ret[i++] = Double.TYPE;
            else if(arg instanceof Boolean) ret[i++] = Boolean.TYPE;
            else if(arg instanceof Character) ret[i++] = Character.TYPE;
            else ret[i++] = arg.getClass();
        }
        return ret;
	}

	public Object construct(Class<?> obj, Object[] a) throws Exception
	{
		Constructor<?> con = null;
		try {
			Class<?>[] p = getParameterType(a);
	
			if (a.length == 0) {
				con = obj.getConstructor();
			} else {
				con = obj.getConstructor(p);
			}
		} catch (Exception e) {
			con = getConstructor(obj, a);
		}
		return con.newInstance(a);
	}

	public Method getMethod(Object obj, String methodName, Object[] a) throws NoSuchMethodException {
		Method m = null;
		// Check for static methods
		if (obj instanceof Class) {
			try {
				m = lookupMethod((Class<?>)obj, methodName, a);
				if (! Modifier.isStatic(m.getModifiers())) {
					m = null;
				}
			} catch (NoSuchMethodException e) {
				m = null;
			}
		}

		// If we're not a static method, carry on as normal
		if (m == null)
			m = lookupMethod(obj.getClass(), methodName, a);

		if (m == null)
			throw new NoSuchMethodException();

		return m;
	}

	private Method lookupMethod(Class<?> cls, String methodName, Object[] a) throws NoSuchMethodException {
		int argc = a.length;

		try {
			Class<?>[] p = getParameterType(a);
			Method m = cls.getMethod(methodName, p);
			if (m != null)
				return m;
		} catch (Exception e) { }

		Method[] methods = cls.getMethods();

		for (Method method: methods) {
			if (methodName.equals(method.getName())) {
				Class<?>[] paramClasses = method.getParameterTypes();
				if (paramClasses.length == argc) {
					boolean correct = true;
					for (int i = 0; i < argc; i++) {
						correct = correct & isCompatible(a[i], paramClasses[i]);
					}
					if (correct) {
						return method;
					}
				}
			}
		}
		throw new NoSuchMethodException();
	}

	private boolean isCompatible(Object object, Class<?> paramType) {
	    if(object == null){
	        return !paramType.isPrimitive();
	    }

	    if(paramType.isInstance(object)){
	        return true;
	    }

	    if(paramType.isPrimitive()){
			try {
				return isWrapperTypeOf(object.getClass(), paramType);
			} catch (Exception e) {
				return false;
			}
		}
	    return false;

	}

	private boolean isWrapperTypeOf(Class<?> candidate, Class<?> primitiveType) throws Exception {
		try {
			return !candidate.isPrimitive() && candidate.getDeclaredField("TYPE").get(null).equals(primitiveType);
		} catch(final NoSuchFieldException e) {
			return false;
		} catch(final Exception e) {
			throw e;
		}
	}

	public Object invoke(Object obj, Method m, Object[] a) throws Exception
	{
		try{
			if (a.length == 0) {
				return m.invoke(obj, (Object[])null);
			}
			return m.invoke(obj, a);
		} catch (InvocationTargetException e) {
			throw (Exception) e.getCause();
		}
	}

	public void setProperty(Object obj, String name, Object argument) throws IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException
	{
		if(obj instanceof Class) {
			((Class<?>) obj).getField(name).set(null, argument);
		} else {
			Field field = obj.getClass().getField(name);
			field.set(obj, argument);
		}
	}

}
