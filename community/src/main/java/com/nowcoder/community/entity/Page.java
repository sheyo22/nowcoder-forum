package com.nowcoder.community.entity;

public class Page {

    //当前页
    private int current = 1;

    //每页显示上限
    private int limit = 10;

    //总页数
    private int rows;

    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if (current >= 1) this.current = current;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if (limit >= 1 && limit <= 100) this.limit = limit;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if (rows >= 0) this.rows = rows;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取当前页起始行
     *
     * @return
     */
    public int getOffset() {
        return (current - 1) * limit;
    }

    /**
     * 获取总页数
     *
     * @return
     */
    public int getTotal() {
        if (rows % limit == 0) return rows / limit;
        return rows / limit + 1;
    }

    public int getFrom() {
        return Integer.max(current - 2, 1);
    }

    public int getTo() {
        return Integer.min(current + 2, getTotal());
    }
}
