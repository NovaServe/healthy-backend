package healthy.lifestyle.backend.admin.service;

import healthy.lifestyle.backend.admin.repository.AdminRepository;
import healthy.lifestyle.backend.workout.dto.BodyPartResponseDto;
import healthy.lifestyle.backend.workout.dto.ExerciseResponseDto;
import healthy.lifestyle.backend.workout.dto.HttpRefResponseDto;
import healthy.lifestyle.backend.workout.model.Exercise;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
@Service
public class AdminServiceImpl implements AdminService{

    private final AdminRepository adminRepository;

    private final ModelMapper modelMapper;

    public AdminServiceImpl(
            AdminRepository adminRepository,
            ModelMapper modelMapper) {
        this.adminRepository = adminRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public List<ExerciseResponseDto> getCustomExercises(){

        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        List<Exercise> exercises = adminRepository.findAllCustomExercises(sort);

        List<ExerciseResponseDto> exercisesResponseDto = exercises.stream()
                .map(elt -> modelMapper.map(elt, ExerciseResponseDto.class))
                .toList();

        return exercisesResponseDto.stream()
                .peek(elt -> {
                    List<BodyPartResponseDto> bodyPartsSorted = elt.getBodyParts().stream()
                            .sorted(Comparator.comparingLong(BodyPartResponseDto::getId))
                            .toList();

                    List<HttpRefResponseDto> httpRefsSorted = elt.getHttpRefs().stream()
                            .sorted(Comparator.comparingLong(HttpRefResponseDto::getId))
                            .toList();

                    elt.setBodyParts(bodyPartsSorted);
                    elt.setHttpRefs(httpRefsSorted);
                })
                .toList();
    }
}
