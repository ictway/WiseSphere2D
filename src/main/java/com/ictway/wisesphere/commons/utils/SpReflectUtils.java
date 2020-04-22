package com.ictway.wisesphere.commons.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.deegree.commons.ows.exception.OWSException;

public class SpReflectUtils {
	
	public static Object getPrivateField(Object obj, String fieldName) {
		
		Object retObj = null;
				
		 try {
			 
			 Field f = obj.getClass().getSuperclass().getDeclaredField(fieldName);
			 f.setAccessible(true);
			 retObj = f.get(obj);
			 
		 } catch ( NoSuchFieldException e) {
		 } catch ( IllegalAccessException e) {
		 }
		 
		 return retObj;
	}
	
	public static Object getInvisibleProtectedField(Object obj , String fieldName) {
		
		Object retObj = null;
				
		 try {
			 
			 Field f = obj.getClass().getSuperclass().getSuperclass().getDeclaredField(fieldName);
			 f.setAccessible(true);
			 retObj = f.get(obj);
			 
		 } catch ( NoSuchFieldException e) {
		 } catch ( IllegalAccessException e) {
		 }
		 
		 return retObj;
	}
	
	
	public static Object setPrivateField(Object obj, String fieldName, Object fieldData) {
		
		Object retObj = null;
				
		 try {
			 
			 Field f = obj.getClass().getSuperclass().getDeclaredField(fieldName);
			 f.setAccessible(true);
			 f.set(obj, fieldData);
			 
		 } catch ( NoSuchFieldException e) {
		 } catch ( IllegalAccessException e) {
		 }
		 
		 return retObj;
	}
	
	
	public static Object callPrivateMethod(Object obj, String methodName, int paramCount, Object... params)  throws OWSException {
		
        Method method;
        Object retObj = null;
        Object[] parameters = new Object[paramCount];
        Class<?>[] classArray = new Class<?>[paramCount];
        
        for (int i = 0; i < paramCount; i++) {
            parameters[i] = params[i];
            classArray[i] = params[i] == null ? String.class : params[i].getClass();
        }
        
        try {
            method = obj.getClass().getSuperclass().getDeclaredMethod(methodName, classArray);
            method.setAccessible(true);
            retObj = method.invoke(obj, params);
        } catch (NoSuchMethodException e) {
        	e.printStackTrace();
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        
        	if (e.getCause() instanceof OWSException) {
        		 throw (OWSException)e.getCause();
        	}else {
        		e.printStackTrace();
        	}
        }
        return retObj;
    }
	
	public static Object callInvisibleProtectedMethod(Object obj, String methodName, int paramCount, Object... params)  throws OWSException {
		
        Method method;
        Object retObj = null;
        Object[] parameters = new Object[paramCount];
        Class<?>[] classArray = new Class<?>[paramCount];
        
        for (int i = 0; i < paramCount; i++) {
            parameters[i] = params[i];
            classArray[i] = params[i] == null ? String.class : params[i].getClass();
        }
        
        try {
            method = obj.getClass().getSuperclass().getSuperclass().getDeclaredMethod(methodName, classArray);
            method.setAccessible(true);
            retObj = method.invoke(obj, params);
        } catch (NoSuchMethodException e) {
        	e.printStackTrace();
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        
        	if (e.getCause() instanceof OWSException) {
        		 throw (OWSException)e.getCause();
        	}else {
        		e.printStackTrace();
        	}
        }
        return retObj;
    }
	
	public static Object callPrivateMethodWithParam(Object obj, String methodName, Class[] args,  int paramCount, Object... params)  throws OWSException {
		
        Method method;
        Object retObj = null;
        Object[] parameters = new Object[paramCount];
        Class<?>[] classArray = new Class<?>[paramCount];
        
        for (int i = 0; i < paramCount; i++) {
            parameters[i] = params[i];
            classArray[i] = params[i] == null ? String.class : params[i].getClass();
        }
        
        try {
            method = obj.getClass().getSuperclass().getDeclaredMethod(methodName, args);
            method.setAccessible(true);
            retObj = method.invoke(obj, params);
        } catch (NoSuchMethodException e) {
        	e.printStackTrace();
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        
        	if (e.getCause() instanceof OWSException) {
        		 throw (OWSException)e.getCause();
        	}else {
        		e.printStackTrace();
        	}
        }
        return retObj;
    }
	
	public static Object callInvisibleProtectedMethodWithParam(Object obj, String methodName, Class[] args, int paramCount, Object... params)  throws OWSException {
		
        Method method;
        Object retObj = null;
        Object[] parameters = new Object[paramCount];
        Class<?>[] classArray = new Class<?>[paramCount];
        
        for (int i = 0; i < paramCount; i++) {
            parameters[i] = params[i];
            classArray[i] = params[i] == null ? String.class : params[i].getClass();
        }
        
        try {
            method = obj.getClass().getSuperclass().getSuperclass().getDeclaredMethod(methodName, args);
            method.setAccessible(true);
            retObj = method.invoke(obj, params);
        } catch (NoSuchMethodException e) {
        	e.printStackTrace();
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        
        	if (e.getCause() instanceof OWSException) {
        		 throw (OWSException)e.getCause();
        	}else {
        		e.printStackTrace();
        	}
        }
        return retObj;
    }
	
	public static Object callStaticPrivateMethod(Class<?> cls, String methodName, int paramCount, Object... params) throws OWSException {
		
        Method method;
        Object retObj = null;
        Object[] parameters = new Object[paramCount];
        Class<?>[] classArray = new Class<?>[paramCount];
        
        for (int i = 0; i < paramCount; i++) {
            parameters[i] = params[i];
            classArray[i] = params[i] == null ? String.class : params[i].getClass();
        }
        
        try {
            method = cls.getDeclaredMethod(methodName, classArray);
            method.setAccessible(true);
            retObj = method.invoke(null, params);
        } catch (NoSuchMethodException e) {
        	e.printStackTrace();
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
            
        	if (e.getCause() instanceof OWSException) {
        		 throw (OWSException)e.getCause();
        	} else {
        		e.printStackTrace();
        	}
        }

        return retObj;
    }	
	

}
