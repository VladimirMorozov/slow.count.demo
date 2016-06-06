package me.vmorozov.slow.count.demo;

import java.io.Serializable;

/**
 * @author vmorozov
 */
public class MyEntity implements Serializable {

    private static final long serialVersionUID = -6808329909501438166L;

    private String filterField;
    private Integer sortField;

    public MyEntity(String filterField, Integer sortField) {
        this.filterField = filterField;
        this.sortField = sortField;
    }

    public String getFilterField() {
        return filterField;
    }

    public void setFilterField(String filterField) {
        this.filterField = filterField;
    }

    public Integer getSortField() {
        return sortField;
    }

    public void setSortField(Integer sortField) {
        this.sortField = sortField;
    }
}
