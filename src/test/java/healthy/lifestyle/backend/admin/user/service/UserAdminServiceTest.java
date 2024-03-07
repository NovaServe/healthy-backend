package healthy.lifestyle.backend.admin.user.service;

import static org.mockito.Mockito.*;

import healthy.lifestyle.backend.admin.user.repository.UserAdminRepository;
import healthy.lifestyle.backend.testutil.TestUtil;
import healthy.lifestyle.backend.user.dto.UserResponseDto;
import healthy.lifestyle.backend.user.model.Country;
import healthy.lifestyle.backend.user.model.Role;
import healthy.lifestyle.backend.user.model.User;
import healthy.lifestyle.backend.user.repository.CountryRepository;
import healthy.lifestyle.backend.user.repository.RoleRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

    @InjectMocks
    UserAdminServiceImpl userAdminService;

    @Mock
    private UserAdminRepository userAdminRepository;

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private RoleRepository roleRepository;

    @Spy
    private TestUtil testUtil;

    @Spy
    ModelMapper modelMapper;

    @Test
    void getUsersWithFilterTest_shouldReturnUsersResponseDtoList_whenValidFilters() {
        // Given
        User user1 = testUtil.createUser(1);
        User user2 = testUtil.createUser(2);

        when(userAdminRepository.findWithFilter(eq(null), anyString(), anyString(), anyString(), eq(null), anyInt()))
                .thenReturn(List.of(user1, user2));

        // When
        List<UserResponseDto> result = userAdminService.getUsersWithFilter(
                null, user1.getUsername(), user1.getEmail(), user1.getFullName(), null, user1.getAge());

        // Then
        verify(roleRepository, never()).findById(anyLong());
        verify(countryRepository, never()).findById(anyLong());
        verify(userAdminRepository, times(1))
                .findWithFilter(isNull(), anyString(), anyString(), anyString(), isNull(), anyInt());
        verify(modelMapper, times(2)).map(any(User.class), eq(UserResponseDto.class));
        Assertions.assertEquals(2, result.size());
    }

    @Test
    void getUsersWithFilterTest_shouldReturnEmptyUserResponseDtoList_whenUsersNotFound() {
        // Given
        Role roleUser = testUtil.createUserRole(1);
        Country country = testUtil.createCountry(1);

        when(countryRepository.findById(anyLong())).thenReturn(Optional.of(country));
        when(roleRepository.findById(anyLong())).thenReturn(Optional.of(roleUser));
        when(userAdminRepository.findWithFilter(
                        any(Role.class), anyString(), anyString(), anyString(), any(Country.class), anyInt()))
                .thenReturn(Collections.emptyList());

        // When
        List<UserResponseDto> result = userAdminService.getUsersWithFilter(
                roleUser.getId(), "NonExistentValue", "NonExistentValue", "NonExistentValue", country.getId(), 20);

        // Then
        verify(userAdminRepository, times(1))
                .findWithFilter(eq(roleUser), anyString(), anyString(), anyString(), eq(country), anyInt());
        verify(roleRepository, times(1)).findById(anyLong());
        verify(countryRepository, times(1)).findById(anyLong());
        verify(modelMapper, times(0)).map(any(User.class), eq(UserResponseDto.class));
        Assertions.assertEquals(0, result.size());
    }
}
