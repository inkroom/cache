package cn.inkroom.cache.spring;

import java.io.Serializable;
import java.util.Objects;

public interface ExampleInterface {
    boolean param(String name, int age);

    boolean param(String name);

    VO vo(String name, int age);

    public static class VO implements Serializable {
        private String name;
        private int age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VO vo = (VO) o;
            return age == vo.age &&
                    Objects.equals(name, vo.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age);
        }
    }

}
