package healthy.lifestyle.backend.workout.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import healthy.lifestyle.backend.users.model.Role;
import healthy.lifestyle.backend.users.model.User;
import healthy.lifestyle.backend.workout.dto.*;
import healthy.lifestyle.backend.workout.model.BodyPart;
import healthy.lifestyle.backend.workout.model.Exercise;
import healthy.lifestyle.backend.workout.model.HttpRef;
import healthy.lifestyle.backend.workout.repository.BodyPartRepository;
import healthy.lifestyle.backend.workout.repository.ExerciseRepository;
import healthy.lifestyle.backend.workout.repository.HttpRefRepository;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

/**
 * @see ExerciseServiceImpl
 */
@ExtendWith(MockitoExtension.class)
class ExerciseServiceImplTest {
    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private BodyPartRepository bodyPartRepository;

    @Mock
    private HttpRefRepository httpRefRepository;

    @InjectMocks
    ExerciseServiceImpl exerciseService;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createExercise_Positive() {
        // Request dto
        BodyPartRequestDto bodyPartRequestDto1 =
                new BodyPartRequestDto.Builder().id(1L).build();
        BodyPartRequestDto bodyPartRequestDto2 =
                new BodyPartRequestDto.Builder().id(2L).build();
        HttpRefRequestDto httpRefRequestDto1 =
                new HttpRefRequestDto.Builder().id(1L).build();
        HttpRefRequestDto httpRefRequestDto2 =
                new HttpRefRequestDto.Builder().id(2L).build();
        CreateExerciseRequestDto requestDto = new CreateExerciseRequestDto.Builder()
                .title("Test exercise title")
                .description("Test exercise description")
                .bodyParts(Set.of(bodyPartRequestDto1, bodyPartRequestDto2))
                .httpRefs(Set.of(httpRefRequestDto1, httpRefRequestDto2))
                .build();

        // Stubs data
        BodyPart bodyPartStub1 = new BodyPart.Builder().id(1L).name("Arms").build();
        BodyPart bodyPartStub2 = new BodyPart.Builder().id(2L).name("Triceps").build();
        HttpRef httpRefStub1 = new HttpRef.Builder()
                .id(1L)
                .name("Media1")
                .ref("https://ref1.com")
                .description("Description1")
                .build();
        HttpRef httpRefStub2 = new HttpRef.Builder()
                .id(2L)
                .name("Media2")
                .ref("https://ref2.com")
                .description("Description2")
                .build();
        Exercise exerciseStub = new Exercise.Builder()
                .id(1L)
                .title("Test exercise title")
                .description("Test exercise description")
                .bodyParts(Set.of(bodyPartStub1, bodyPartStub2))
                .httpRefs(Set.of(httpRefStub1, httpRefStub2))
                .build();

        // Expected response dto
        BodyPartResponseDto bodyPartResponseDto1 =
                new BodyPartResponseDto.Builder().id(1L).name("Body part 1").build();
        BodyPartResponseDto bodyPartResponseDto2 =
                new BodyPartResponseDto.Builder().id(2L).name("Body part 2").build();
        HttpRefResponseDto httpRefResponseDto1 = new HttpRefResponseDto.Builder()
                .id(1L)
                .name("Media1")
                .ref("https://ref1.com")
                .description("Description1")
                .build();
        HttpRefResponseDto httpRefResponseDto2 = new HttpRefResponseDto.Builder()
                .id(2L)
                .name("Media2")
                .ref("https://ref2.com")
                .description("Description2")
                .build();

        long userId = 1L;

        // Stubs
        when(bodyPartRepository.existsById(anyLong())).thenReturn(true);
        when(bodyPartRepository.getReferenceById(1L)).thenReturn(bodyPartStub1);
        when(bodyPartRepository.getReferenceById(2L)).thenReturn(bodyPartStub2);
        when(httpRefRepository.existsById(anyLong())).thenReturn(true);
        when(httpRefRepository.getReferenceById(1L)).thenReturn(httpRefStub1);
        when(httpRefRepository.getReferenceById(2L)).thenReturn(httpRefStub2);
        when(exerciseRepository.findByTitleAndUserId("Test exercise title", userId))
                .thenReturn(Optional.empty());
        when(exerciseRepository.save(any(Exercise.class))).thenReturn(exerciseStub);

        // Test
        CreateExerciseResponseDto actual = exerciseService.createExercise(requestDto, userId);
        assertNotNull(actual.getId());
        assertEquals(requestDto.getTitle(), actual.getTitle());
        assertEquals(requestDto.getDescription(), actual.getDescription());

        assertEquals(requestDto.getBodyParts().size(), actual.getBodyParts().size());
        List<BodyPartRequestDto> expectedBodyParts = requestDto.getBodyParts().stream()
                .sorted(Comparator.comparingLong(BodyPartRequestDto::getId))
                .toList();
        List<BodyPartResponseDto> actualBodyParts = actual.getBodyParts().stream()
                .sorted(Comparator.comparingLong(BodyPartResponseDto::getId))
                .toList();

        assertEquals(expectedBodyParts.size(), actualBodyParts.size());
        assertEquals(expectedBodyParts.get(0).getId(), actualBodyParts.get(0).getId());
        assertEquals(bodyPartStub1.getName(), actualBodyParts.get(0).getName());
        assertEquals(expectedBodyParts.get(1).getId(), actualBodyParts.get(1).getId());
        assertEquals(bodyPartStub2.getName(), actualBodyParts.get(1).getName());

        assertEquals(requestDto.getHttpRefs().size(), actual.getHttpRefs().size());
        List<HttpRefRequestDto> expectedHttpRefs = requestDto.getHttpRefs().stream()
                .sorted(Comparator.comparingLong(HttpRefRequestDto::getId))
                .toList();
        List<HttpRefResponseDto> actualHttpRefs = actual.getHttpRefs().stream()
                .sorted(Comparator.comparingLong(HttpRefResponseDto::getId))
                .toList();
        assertEquals(expectedHttpRefs.size(), actualHttpRefs.size());
        assertEquals(expectedHttpRefs.get(0).getId(), actualHttpRefs.get(0).getId());
        assertEquals(httpRefStub1.getName(), actualHttpRefs.get(0).getName());
        assertEquals(httpRefStub1.getRef(), actualHttpRefs.get(0).getRef());
        assertEquals(httpRefStub1.getDescription(), actualHttpRefs.get(0).getDescription());
        assertEquals(expectedHttpRefs.get(1).getId(), actualHttpRefs.get(1).getId());
        assertEquals(httpRefStub2.getName(), actualHttpRefs.get(1).getName());
        assertEquals(httpRefStub2.getRef(), actualHttpRefs.get(1).getRef());
        assertEquals(httpRefStub2.getDescription(), actualHttpRefs.get(1).getDescription());
    }

    @Test
    void getExercises_CustomOnly() {
        // Test data
        BodyPart bodyPart1 = new BodyPart.Builder().id(1L).name("Body part 1").build();
        BodyPart bodyPart2 = new BodyPart.Builder().id(2L).name("Body part 2").build();
        BodyPart bodyPart3 = new BodyPart.Builder().id(3L).name("Body part 3").build();
        HttpRef httpRef1 =
                new HttpRef.Builder().id(1L).name("Ref1").description("Desc1").build();
        HttpRef httpRef2 =
                new HttpRef.Builder().id(2L).name("Ref2").description("Desc2").build();
        HttpRef httpRef3 =
                new HttpRef.Builder().id(3L).name("Ref3").description("Desc3").build();
        // Default exercise
        Exercise exercise1 = new Exercise.Builder()
                .id(1L)
                .isCustom(false)
                .title("Title 1")
                .description("Description")
                .bodyParts(Set.of(bodyPart1))
                .httpRefs(Set.of(httpRef1))
                .build();
        // Custom exercises
        Exercise exercise2 = new Exercise.Builder()
                .id(2L)
                .isCustom(true)
                .title("Title 2")
                .description("Description")
                .bodyParts(Set.of(bodyPart1, bodyPart2))
                .httpRefs(Set.of(httpRef1, httpRef2))
                .build();
        Exercise exercise3 = new Exercise.Builder()
                .id(3L)
                .isCustom(true)
                .title("Title 3")
                .description("Description")
                .bodyParts(Set.of(bodyPart3))
                .httpRefs(Set.of(httpRef3))
                .build();
        Exercise exercise4 = new Exercise.Builder()
                .id(4L)
                .isCustom(true)
                .title("Title 4")
                .description("Description")
                .bodyParts(Set.of(bodyPart1))
                .httpRefs(null)
                .build();
        Role role = new Role.Builder().id(1L).name("ROLE_USER").build();
        // Test user
        User user1 = new User.Builder()
                .id(1L)
                .fullName("Full Name")
                .username("username-one")
                .email("user1@email.com")
                .password("password1")
                .role(role)
                .exercises(Set.of(exercise2, exercise3))
                .build();
        // Other user to imitate real scenario
        User user2 = new User.Builder()
                .id(2L)
                .fullName("Full Name")
                .username("username-two")
                .email("user2@email.com")
                .password("password2")
                .role(role)
                .exercises(Set.of(exercise4))
                .build();

        Sort sort = Sort.by(Sort.Direction.ASC, "id");

        // Stubs
        when(exerciseRepository.findByUserId(user1.getId(), sort)).thenReturn(List.of(exercise2, exercise3));

        // Test
        GetExercisesResponseDto responseDto = exerciseService.getExercises(user1.getId(), true);

        verify((exerciseRepository), times(1)).findByUserId(user1.getId(), sort);
        verify(exerciseRepository, times(0)).findAllDefault(sort);

        assertEquals(2, responseDto.getExercises().size());

        // Custom exercise
        assertEquals(exercise2.getId(), responseDto.getExercises().get(0).getId());
        assertEquals(exercise2.getTitle(), responseDto.getExercises().get(0).getTitle());
        assertEquals(
                exercise2.getDescription(), responseDto.getExercises().get(0).getDescription());
        assertEquals(
                exercise2.getBodyParts().size(),
                responseDto.getExercises().get(0).getBodyParts().size());

        List<BodyPart> bodyPartsExpected = exercise2.getBodyParts().stream().toList();
        List<BodyPartResponseDto> bodyPartsActual =
                responseDto.getExercises().get(0).getBodyParts().stream().toList();
        assertEquals(bodyPartsExpected.size(), bodyPartsActual.size());
        assertEquals(bodyPartsExpected.get(0).getId(), bodyPartsActual.get(0).getId());
        assertEquals(bodyPartsExpected.get(0).getName(), bodyPartsActual.get(0).getName());
        assertEquals(bodyPartsExpected.get(1).getId(), bodyPartsActual.get(1).getId());
        assertEquals(bodyPartsExpected.get(1).getName(), bodyPartsActual.get(1).getName());

        List<HttpRef> httpRefsExpected = exercise2.getHttpRefs().stream().toList();
        List<HttpRefResponseDto> httpRefsActual =
                responseDto.getExercises().get(0).getHttpRefs().stream().toList();
        assertEquals(httpRefsExpected.size(), httpRefsActual.size());
        assertEquals(httpRefsExpected.get(0).getId(), httpRefsActual.get(0).getId());
        assertEquals(httpRefsExpected.get(0).getName(), httpRefsActual.get(0).getName());
        assertEquals(httpRefsExpected.get(0).getRef(), httpRefsActual.get(0).getRef());
        assertEquals(
                httpRefsExpected.get(0).getDescription(), httpRefsActual.get(0).getDescription());
        assertEquals(httpRefsExpected.get(1).getId(), httpRefsActual.get(1).getId());
        assertEquals(httpRefsExpected.get(1).getName(), httpRefsActual.get(1).getName());
        assertEquals(httpRefsExpected.get(1).getRef(), httpRefsActual.get(1).getRef());
        assertEquals(
                httpRefsExpected.get(1).getDescription(), httpRefsActual.get(1).getDescription());

        // Custom exercise
        assertEquals(exercise3.getId(), responseDto.getExercises().get(1).getId());
        assertEquals(exercise3.getTitle(), responseDto.getExercises().get(1).getTitle());
        assertEquals(
                exercise3.getDescription(), responseDto.getExercises().get(1).getDescription());
        assertEquals(
                exercise3.getBodyParts().size(),
                responseDto.getExercises().get(1).getBodyParts().size());
        bodyPartsExpected = exercise3.getBodyParts().stream().toList();
        bodyPartsActual =
                responseDto.getExercises().get(1).getBodyParts().stream().toList();
        assertEquals(bodyPartsExpected.size(), bodyPartsActual.size());
        assertEquals(bodyPartsExpected.get(0).getId(), bodyPartsActual.get(0).getId());
        assertEquals(bodyPartsExpected.get(0).getName(), bodyPartsActual.get(0).getName());

        httpRefsExpected = exercise3.getHttpRefs().stream().toList();
        httpRefsActual =
                responseDto.getExercises().get(1).getHttpRefs().stream().toList();
        assertEquals(httpRefsExpected.size(), httpRefsActual.size());
        assertEquals(httpRefsExpected.get(0).getId(), httpRefsActual.get(0).getId());
        assertEquals(httpRefsExpected.get(0).getName(), httpRefsActual.get(0).getName());
        assertEquals(httpRefsExpected.get(0).getRef(), httpRefsActual.get(0).getRef());
        assertEquals(
                httpRefsExpected.get(0).getDescription(), httpRefsActual.get(0).getDescription());
    }

    @Test
    void getExercises_DefaultAndCustom() {
        // Test data
        BodyPart bodyPart1 = new BodyPart.Builder().id(1L).name("Body part 1").build();
        BodyPart bodyPart2 = new BodyPart.Builder().id(2L).name("Body part 2").build();
        BodyPart bodyPart3 = new BodyPart.Builder().id(3L).name("Body part 3").build();
        HttpRef httpRef1 =
                new HttpRef.Builder().id(1L).name("Ref1").description("Desc1").build();
        HttpRef httpRef2 =
                new HttpRef.Builder().id(2L).name("Ref2").description("Desc2").build();
        HttpRef httpRef3 =
                new HttpRef.Builder().id(3L).name("Ref3").description("Desc3").build();
        // Default exercise
        Exercise exercise1 = new Exercise.Builder()
                .id(1L)
                .isCustom(false)
                .title("Title 1")
                .description("Description")
                .bodyParts(Set.of(bodyPart1))
                .httpRefs(Set.of(httpRef1))
                .build();
        // Custom exercises
        Exercise exercise2 = new Exercise.Builder()
                .id(2L)
                .isCustom(true)
                .title("Title 2")
                .description("Description")
                .bodyParts(Set.of(bodyPart1, bodyPart2))
                .httpRefs(Set.of(httpRef1, httpRef2))
                .build();
        Exercise exercise3 = new Exercise.Builder()
                .id(3L)
                .isCustom(true)
                .title("Title 3")
                .description("Description")
                .bodyParts(Set.of(bodyPart3))
                .httpRefs(Set.of(httpRef3))
                .build();
        Exercise exercise4 = new Exercise.Builder()
                .id(4L)
                .isCustom(true)
                .title("Title 4")
                .description("Description")
                .bodyParts(Set.of(bodyPart1))
                .httpRefs(null)
                .build();
        Role role = new Role.Builder().id(1L).name("ROLE_USER").build();
        // Test user
        User user1 = new User.Builder()
                .id(1L)
                .fullName("Full Name")
                .username("username-one")
                .email("user1@email.com")
                .password("password1")
                .role(role)
                .exercises(Set.of(exercise2, exercise3))
                .build();
        // Other user to imitate real scenario
        User user2 = new User.Builder()
                .id(2L)
                .fullName("Full Name")
                .username("username-two")
                .email("user2@email.com")
                .password("password2")
                .role(role)
                .exercises(Set.of(exercise4))
                .build();

        Sort sort = Sort.by(Sort.Direction.ASC, "id");

        // Stubs
        when(exerciseRepository.findAllDefault(sort)).thenReturn(List.of(exercise1));
        when(exerciseRepository.findByUserId(user1.getId(), sort)).thenReturn(List.of(exercise2, exercise3));

        // Test
        GetExercisesResponseDto responseDto = exerciseService.getExercises(user1.getId(), false);

        verify(exerciseRepository, times(1)).findAllDefault(sort);
        verify((exerciseRepository), times(1)).findByUserId(user1.getId(), sort);

        assertEquals(3, responseDto.getExercises().size());

        // Default exercise
        assertEquals(exercise1.getId(), responseDto.getExercises().get(0).getId());
        assertEquals(exercise1.getTitle(), responseDto.getExercises().get(0).getTitle());
        assertEquals(
                exercise1.getDescription(), responseDto.getExercises().get(0).getDescription());
        assertEquals(
                exercise1.getBodyParts().size(),
                responseDto.getExercises().get(0).getBodyParts().size());
        List<BodyPart> bodyPartsExpected = exercise1.getBodyParts().stream().toList();
        List<BodyPartResponseDto> bodyPartsActual =
                responseDto.getExercises().get(0).getBodyParts().stream().toList();
        assertEquals(bodyPartsExpected.size(), bodyPartsActual.size());
        assertEquals(bodyPartsExpected.get(0).getId(), bodyPartsActual.get(0).getId());
        assertEquals(bodyPartsExpected.get(0).getName(), bodyPartsActual.get(0).getName());

        List<HttpRef> httpRefsExpected = exercise1.getHttpRefs().stream().toList();
        List<HttpRefResponseDto> httpRefsActual =
                responseDto.getExercises().get(0).getHttpRefs().stream().toList();
        assertEquals(httpRefsExpected.size(), httpRefsActual.size());
        assertEquals(httpRefsExpected.get(0).getId(), httpRefsActual.get(0).getId());
        assertEquals(httpRefsExpected.get(0).getName(), httpRefsActual.get(0).getName());
        assertEquals(httpRefsExpected.get(0).getRef(), httpRefsActual.get(0).getRef());
        assertEquals(
                httpRefsExpected.get(0).getDescription(), httpRefsActual.get(0).getDescription());

        // Custom exercise
        assertEquals(exercise2.getId(), responseDto.getExercises().get(1).getId());
        assertEquals(exercise2.getTitle(), responseDto.getExercises().get(1).getTitle());
        assertEquals(
                exercise2.getDescription(), responseDto.getExercises().get(1).getDescription());
        assertEquals(
                exercise2.getBodyParts().size(),
                responseDto.getExercises().get(1).getBodyParts().size());
        bodyPartsExpected = exercise2.getBodyParts().stream().toList();
        bodyPartsActual =
                responseDto.getExercises().get(1).getBodyParts().stream().toList();
        assertEquals(bodyPartsExpected.size(), bodyPartsActual.size());
        assertEquals(bodyPartsExpected.get(0).getId(), bodyPartsActual.get(0).getId());
        assertEquals(bodyPartsExpected.get(0).getName(), bodyPartsActual.get(0).getName());
        assertEquals(bodyPartsExpected.get(1).getId(), bodyPartsActual.get(1).getId());
        assertEquals(bodyPartsExpected.get(1).getName(), bodyPartsActual.get(1).getName());

        httpRefsExpected = exercise2.getHttpRefs().stream().toList();
        httpRefsActual =
                responseDto.getExercises().get(1).getHttpRefs().stream().toList();
        assertEquals(httpRefsExpected.size(), httpRefsActual.size());
        assertEquals(httpRefsExpected.get(0).getId(), httpRefsActual.get(0).getId());
        assertEquals(httpRefsExpected.get(0).getName(), httpRefsActual.get(0).getName());
        assertEquals(httpRefsExpected.get(0).getRef(), httpRefsActual.get(0).getRef());
        assertEquals(
                httpRefsExpected.get(0).getDescription(), httpRefsActual.get(0).getDescription());
        assertEquals(httpRefsExpected.get(1).getId(), httpRefsActual.get(1).getId());
        assertEquals(httpRefsExpected.get(1).getName(), httpRefsActual.get(1).getName());
        assertEquals(httpRefsExpected.get(1).getRef(), httpRefsActual.get(1).getRef());
        assertEquals(
                httpRefsExpected.get(1).getDescription(), httpRefsActual.get(1).getDescription());

        // Custom exercise
        assertEquals(exercise3.getId(), responseDto.getExercises().get(2).getId());
        assertEquals(exercise3.getTitle(), responseDto.getExercises().get(2).getTitle());
        assertEquals(
                exercise3.getDescription(), responseDto.getExercises().get(2).getDescription());
        assertEquals(
                exercise3.getBodyParts().size(),
                responseDto.getExercises().get(2).getBodyParts().size());
        bodyPartsExpected = exercise3.getBodyParts().stream().toList();
        bodyPartsActual =
                responseDto.getExercises().get(2).getBodyParts().stream().toList();
        assertEquals(bodyPartsExpected.size(), bodyPartsActual.size());
        assertEquals(bodyPartsExpected.get(0).getId(), bodyPartsActual.get(0).getId());
        assertEquals(bodyPartsExpected.get(0).getName(), bodyPartsActual.get(0).getName());

        httpRefsExpected = exercise3.getHttpRefs().stream().toList();
        httpRefsActual =
                responseDto.getExercises().get(2).getHttpRefs().stream().toList();
        assertEquals(httpRefsExpected.size(), httpRefsActual.size());
        assertEquals(httpRefsExpected.get(0).getId(), httpRefsActual.get(0).getId());
        assertEquals(httpRefsExpected.get(0).getName(), httpRefsActual.get(0).getName());
        assertEquals(httpRefsExpected.get(0).getRef(), httpRefsActual.get(0).getRef());
        assertEquals(
                httpRefsExpected.get(0).getDescription(), httpRefsActual.get(0).getDescription());
    }
}
