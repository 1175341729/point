package com.point.cart.common.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class AbsExample {
	
	private static final Log log = LogFactory.getLog(AbsExample.class);

	// 分页参数
	private Integer offset;
	private Integer limit;

	/**
	 * @return the offset
	 */
	public Integer getOffset() {
		return offset;
	}

	/**
	 * @param offset
	 *            the offset to set
	 */
	public void setOffset(Integer offset) {
		this.offset = offset;
	}

	/**
	 * @return the limit
	 */
	public Integer getLimit() {
		return limit;
	}

	/**
	 * @param limit
	 *            the limit to set
	 */
	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	/**
	 * 执行方法
	 * 
	 * @param field
	 * @param condition
	 * @param values
	 */
	public static void execute(Object criteria, String field, String condition, Object... values) {
		Method[] methods = criteria.getClass().getMethods();
		for (Method method : methods) {
			if (method.getName().equalsIgnoreCase("and" + field + condition)) {
				try {
					Class<?>[] pt = method.getParameterTypes();
					Object[] vals = new Object[pt.length];
					for(int i = 0; i < pt.length; i++) {
						vals[i] = cast(pt[i], values[i]);
					}
					method.invoke(criteria, vals);
				} catch (IllegalAccessException e) {
					log.error("方法不能访问", e);
				} catch (IllegalArgumentException e) {
					log.error("方法参数不合法", e);
				} catch (InvocationTargetException e) {
					log.error("调用发生错误", e);
				}
				break;
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private static Object cast(Class dst, Object v) {
		if(v.getClass() == Integer.class && dst == Long.class) {
			return new Long(((Integer)v).longValue());
		}
		return v;
	}
}
