package com.iyundao.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iyundao.base.utils.ClassUtils;
import com.iyundao.base.utils.TimeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.tree.AbstractEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.groups.Default;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

/**
 * @ClassName: User
 * @project: IYunDao
 * @author: 念
 * @Date: 8:46 2019/8/5
 * @Description: Entity - 基类
 * @Version: V1.0
 */
@MappedSuperclass
public abstract class BaseEntity<ID extends Serializable> implements Serializable {

    /**
     * "ID"属性名称
     */
    public static final String ID_PROPERTY_NAME = "id";
    /**
     * "创建日期"属性名称
     */
    public static final String CREATED_DATE_PROPERTY_NAME = "createdDate";
    /**
     * "最后修改日期"属性名称
     */
    public static final String LAST_MODIFIED_DATE_PROPERTY_NAME = "lastModifiedDate";

    /**
     * "版本"属性名称
     */
    public static final String VERSION_PROPERTY_NAME = "version";
    private static final long serialVersionUID = -67188388306700736L;
    @Transient
    protected final transient Log logger = LogFactory.getLog(this.getClass());
    /**
     * 创建日期
     */
    @JsonIgnore
    @Column(name = "CREATEDATE", nullable = false, updatable = false, length = 16)
    private String createdDate;
    /**
     * 最后修改日期
     */
    @JsonIgnore
    @Column(name = "LASTMODIFIEDTIME", nullable = false, length = 16)
    private String lastModifiedDate;
    /**
     * ID
     */
    @Id
    @GeneratedValue(generator = "jpa-uuid")
    @GenericGenerator(name = "jpa-uuid",
            strategy = "uuid")
    @Column(name = "ID", length = 32)
    private ID id;
    /**
     * 版本
     */
    @JsonIgnore
    @Version
    @Column(name = "VERSION", nullable = false, columnDefinition = "bigint(20) default 1")
    private Long version;

    /**
     * 获取ID
     *
     * @return ID
     */
    public ID getId() {
        return (ID) id.toString().replace("-", "");
    }

    /**
     * 设置ID
     *
     * @param id ID
     */
    private void setId(ID id) {
        this.id = (ID)id.toString().replace("-", "");
    }

    /**
     * 获取创建日期
     *
     * @return 创建日期
     */
    public String getCreatedDate() {
        if (StringUtils.isBlank(this.createdDate)) {
            this.createdDate = TimeUtils.nowTime();
        }
        return createdDate;
    }

    /**
     * 设置创建日期
     *
     * @param createdDate 创建日期
     */
    public void setCreatedDate(Date createdDate) {
        this.createdDate = TimeUtils.convertTime(createdDate, "yyyyMMddHHmmss");
    }

    /**
     * 获取最后修改日期
     *
     * @return 最后修改日期
     */
    public String getLastModifiedDate() {
        if (StringUtils.isBlank(this.lastModifiedDate)) {
            this.lastModifiedDate = TimeUtils.nowTime();
        }
        return lastModifiedDate;
    }

    /**
     * 设置最后修改日期
     *
     * @param lastModifiedDate 最后修改日期
     */
    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = TimeUtils.convertTime(lastModifiedDate, "yyyyMMddHHmmss");
    }

    /**
     * 获取版本
     *
     * @return 版本
     */
    public Long getVersion() {
        return version;
    }

    /**
     * 设置版本
     *
     * @param version 版本
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     * 判断是否为新建对象
     *
     * @return 是否为新建对象
     */
    @Transient
    public boolean isNew() {
        return getId() == null;
    }

    /**
     * 重写toString方法
     *
     * @return 字符串
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(this.getClass().getSimpleName());
        builder.append("[");
        Iterator var3 = ClassUtils.getDeclaredFieldsWithSuper(this.getClass()).values().iterator();

        while (var3.hasNext()) {
            Field field = (Field) var3.next();
            Class<? extends Object> typeClazz = field.getType();
            if (!BaseEntity.class.isAssignableFrom(typeClazz) && !Collection.class.isAssignableFrom(typeClazz) && !Map.class.isAssignableFrom(typeClazz)) {
                int modifiers = field.getModifiers();
                if (field.getName().indexOf(36) == -1 && !Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers)) {
                    builder.append(field.getName()+"="+ ClassUtils.forceGetProperty(this, field.getName())+", ");
                }
            }
        }
        return builder.substring(0, builder.length() -2) + "]";
    }
    
    /**
     * 重写equals方法
     *
     * @param other 对象
     * @return 是否相等
     */
    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        } else if (this.getClass().isInstance(other) && other.getClass().isInstance(this)) {
            EqualsBuilder builder = new EqualsBuilder();
            Iterator var4 = ClassUtils.getDeclaredFieldsWithSuper(this.getClass()).values().iterator();

            while (var4.hasNext()) {
                Field field = (Field) var4.next();
                Class<? extends Object> typeClazz = field.getType();
                if (!AbstractEntity.class.isAssignableFrom(typeClazz) && !Collection.class.isAssignableFrom(typeClazz) && !Map.class.isAssignableFrom(typeClazz)) {
                    int modifiers = field.getModifiers();
                    if (field.getName().indexOf(36) == -1 && !Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers)) {
                        builder.append(ClassUtils.forceGetProperty(this, field.getName()), ClassUtils.forceGetProperty(other, field.getName()));
                    }
                }
            }

            return builder.isEquals();
        } else {
            return false;
        }
    }

    /**
     * 重写hashCode方法
     *
     * @return HashCode
     */
    @Override
    public int hashCode() {
        int hashCode = 17;
        hashCode += getId() != null ? getId().hashCode() * 31 : 0;
        return hashCode;
    }

    /**
     * 保存验证组
     */
    public interface Save extends Default {


    }

    /**
     * 更新验证组
     */
    public interface Update extends Default {

    }

}