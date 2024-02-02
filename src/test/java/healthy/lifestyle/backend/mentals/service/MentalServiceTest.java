package healthy.lifestyle.backend.mentals.service;

import static org.mockito.Mockito.*;

import healthy.lifestyle.backend.mentals.repository.MentalRepository;
import healthy.lifestyle.backend.mentals.repository.MentalTypeRepository;
import healthy.lifestyle.backend.users.service.UserServiceImpl;
import healthy.lifestyle.backend.util.DtoUtil;
import healthy.lifestyle.backend.util.TestUtil;
import healthy.lifestyle.backend.workout.repository.HttpRefRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
class MentalServiceTest {
    @Mock
    private MentalRepository mentalRepository;

    @Mock
    private MentalTypeRepository mentalTypeRepository;

    @Mock
    private HttpRefRepository httpRefRepository;

    @Mock
    private UserServiceImpl userService;

    @Spy
    ModelMapper modelMapper;

    @InjectMocks
    MentalServiceImpl mentalService;

    TestUtil testUtil = new TestUtil();

    DtoUtil dtoUtil = new DtoUtil();
}
