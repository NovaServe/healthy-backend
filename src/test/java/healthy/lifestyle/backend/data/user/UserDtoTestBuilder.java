package healthy.lifestyle.backend.data.user;

import healthy.lifestyle.backend.users.dto.UserUpdateRequestDto;
import java.lang.reflect.Field;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
public class UserDtoTestBuilder {
    public <T> UserDtoWrapper<T> getWrapper(Class<T> type) {
        return new UserDtoWrapper<>(type);
    }

    public static class UserDtoWrapper<T> {
        private T dto;

        private Class<T> type;

        private String seed;

        private String username;

        private String email;

        private String fullName;

        private Long countryId;

        private Integer age;

        private String password;

        private String confirmPassword;

        public UserDtoWrapper(Class<T> type) {
            this.type = type;
        }

        public UserDtoWrapper<T> setSeed(String seed) {
            this.seed = seed;
            return this;
        }

        public UserDtoWrapper<T> setUsername(String username) {
            this.username = username;
            return this;
        }

        public UserDtoWrapper<T> setEmail(String email) {
            this.email = email;
            return this;
        }

        public UserDtoWrapper<T> setFullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public UserDtoWrapper<T> setCountryId(Long countryId) {
            this.countryId = countryId;
            return this;
        }

        public UserDtoWrapper<T> setAge(Integer age) {
            this.age = age;
            return this;
        }

        public UserDtoWrapper<T> setPassword(String password) {
            this.password = password;
            return this;
        }

        public UserDtoWrapper<T> setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
            return this;
        }

        public void buildUserUpdateRequestDto() {
            this.dto = this.type.cast(UserUpdateRequestDto.builder()
                    .username(this.username)
                    .email(this.email)
                    .fullName(this.fullName)
                    .countryId(this.countryId)
                    .age(this.age)
                    .password(this.password)
                    .confirmPassword(this.confirmPassword)
                    .build());
        }

        public T getDto() {
            return this.dto;
        }

        public void setFieldValue(String fieldName, Object value) throws IllegalAccessException, NoSuchFieldException {
            if (this.dto == null) throw new IllegalStateException("Not all required parameters are set");

            T obj = this.dto;
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        }

        public Object getFieldValue(String fieldName) throws IllegalAccessException, NoSuchFieldException {
            if (this.dto == null) throw new IllegalStateException("Not all required parameters are set");

            T obj = this.dto;
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        }
    }
}
