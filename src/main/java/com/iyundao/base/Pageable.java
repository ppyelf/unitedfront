package com.iyundao.base;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: Pageable
 * @project: IYunDao
 * @author: 念
 * @Date: 2019/5/13 16:24
 * @Description: 分页信息
 * @Version: V2.0
 */
public class Pageable implements Serializable {

	private static final long serialVersionUID = -3930180379790344299L;

	/**
	 * 默认页码
	 */
	private static final int DEFAULT_PAGE_NUMBER = 0;

	/**
	 * 默认每页记录数
	 */
	private static final int DEFAULT_PAGE_SIZE = 20;

	/**
	 * 最大每页记录数
	 */
	private static final int MAX_PAGE_SIZE = 1000;

	/**
	 * 页码
	 */
	private int pageNumber = DEFAULT_PAGE_NUMBER;

	/**
	 * 每页记录数
	 */
	private int pageSize = DEFAULT_PAGE_SIZE;

	/**
	 * 搜索属性
	 */
	private String searchKey;

	/**
	 * 搜索值
	 */
	private String searchValue;

	/**
	 * 排序属性
	 */
	private String orderProperty;

	/**
	 * 排序方向
	 */
	private Order.Direction orderDirection;

	/**
	 * 排序
	 */
	private List<Order> orders = new ArrayList<>();

	/**
	 * 构造方法
	 */
	public Pageable() {
	}

	/**
	 * 构造方法
	 * 
	 * @param pageNumber
	 *            页码
	 * @param pageSize
	 *            每页记录数
	 */
	public Pageable(Integer pageNumber, Integer pageSize) {
		if (pageNumber != null && pageNumber >= 1) {
			this.pageNumber = pageNumber;
		}
		if (pageSize != null && pageSize >= 1 && pageSize <= MAX_PAGE_SIZE) {
			this.pageSize = pageSize;
		}
	}

	/**
	 * 获取页码
	 * 
	 * @return 页码
	 */
	public int getPageNumber() {
		return pageNumber;
	}

	/**]pif=9`7	 * 设置页码
	 * 
	 * @param pageNumber
	 *            页码
	 */
	public void setPageNumber(int pageNumber) {
		if (pageNumber < 1) {
			pageNumber = DEFAULT_PAGE_NUMBER;
		}
		this.pageNumber = pageNumber;
	}

	/**
	 * 获取每页记录数
	 * 
	 * @return 每页记录数
	 */
	public int getPageSize() {
		return pageSize;
	}

	/**
	 * 设置每页记录数
	 * 
	 * @param pageSize
	 *            每页记录数
	 */
	public void setPageSize(int pageSize) {
		if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
			pageSize = DEFAULT_PAGE_SIZE;
		}
		this.pageSize = pageSize;
	}

    public String getSearchKey() {
        return searchKey;
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

    /**
	 * 获取搜索值
	 * 
	 * @return 搜索值
	 */
	public String getSearchValue() {
		return searchValue;
	}

	/**
	 * 设置搜索值
	 * 
	 * @param searchValue
	 *            搜索值
	 */
	public void setSearchValue(String searchValue) {
		this.searchValue = searchValue;
	}

	/**
	 * 获取排序属性
	 * 
	 * @return 排序属性
	 */
	public String getOrderProperty() {
		return orderProperty;
	}

	/**
	 * 设置排序属性
	 * 
	 * @param orderProperty
	 *            排序属性
	 */
	public void setOrderProperty(String orderProperty) {
		this.orderProperty = orderProperty;
	}

	/**
	 * 获取排序方向
	 * 
	 * @return 排序方向
	 */
	public Order.Direction getOrderDirection() {
		return orderDirection;
	}

	/**
	 * 设置排序方向
	 * 
	 * @param orderDirection
	 *            排序方向
	 */
	public void setOrderDirection(Order.Direction orderDirection) {
		this.orderDirection = orderDirection;
	}

	/**
	 * 获取排序
	 * 
	 * @return 排序
	 */
	public List<Order> getOrders() {
		return orders;
	}

	/**
	 * 设置排序
	 * 
	 * @param orders
	 *            排序
	 */
	public void setOrders(List<Order> orders) {
		this.orders = orders;
	}

	/**
	 * 重写equals方法
	 * 
	 * @param obj
	 *            对象
	 * @return 是否相等
	 */
	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	/**
	 * 重写hashCode方法
	 * 
	 * @return HashCode
	 */
	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

}