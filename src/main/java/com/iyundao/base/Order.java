package com.iyundao.base;


import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * @ClassName: Order
 * @project: IYunDao
 * @author: 念
 * @Date: 2019/5/13 16:24
 * @Description: 排序
 * @Version: V2.0
 */
public class Order implements Serializable {

    private static final long serialVersionUID = -3078342809727773232L;
    /**
     * 默认方向
     */
    private static final Direction DEFAULT_DIRECTION = Direction.desc;
    /**
     * 属性
     */
    private String property;
    /**
     * 方向
     */
    private Direction direction = DEFAULT_DIRECTION;

    /**
     * 构造方法
     */
    public Order() {
    }

    /**
     * 构造方法
     *
     * @param property
     *            属性
     * @param direction
     *            方向
     */
    public Order(String property, Direction direction) {
        this.property = property;
        this.direction = direction;
    }

    /**
     * 返回递增排序
     *
     * @param property
     *            属性
     * @return 递增排序
     */
    public static Order asc(String property) {
        return new Order(property, Direction.asc);
    }

    /**
     * 返回递减排序
     *
     * @param property
     *            属性
     * @return 递减排序
     */
    public static Order desc(String property) {
        return new Order(property, Direction.desc);
    }

    /**
     * 获取属性
     *
     * @return 属性
     */
    public String getProperty() {
        return property;
    }

    /**
     * 设置属性
     *
     * @param property
     *            属性
     */
    public void setProperty(String property) {
        this.property = property;
    }

    /**
     * 获取方向
     *
     * @return 方向
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * 设置方向
     *
     * @param direction
     *            方向
     */
    public void setDirection(Direction direction) {
        this.direction = direction;
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

    /**
     * 方向
     */
    public enum Direction {

        /**
         * 递增
         */
        asc,

        /**
         * 递减
         */
        desc
    }

}