package cn.inkroom.cache.example.bean;

import java.util.Objects;

/**
 * @author 墨盒
 * @date 2019/10/26
 */
public class Cache
{
    private int id;
    private String name;
    private int type;
    private int age;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "Cache{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", age=" + age +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cache cache = (Cache) o;
        return id == cache.id &&
                type == cache.type &&
                age == cache.age &&
                name.equals(cache.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, type, age);
    }
}
