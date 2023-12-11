package healthy.lifestyle.backend.data.country;

import healthy.lifestyle.backend.users.model.Country;
import healthy.lifestyle.backend.users.repository.CountryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
public class CountryJpaTestBuilder {
    @Autowired
    private CountryRepository countryRepository;

    public CountryTestWrapper getWrapper() {
        return new CountryTestWrapper(this.countryRepository);
    }

    public static class CountryTestWrapper implements CountryTestWrapperBase {
        private Long idOrSeed;

        private Country country;

        private CountryRepository countryRepository;

        public CountryTestWrapper(CountryRepository countryRepository) {
            this.countryRepository = countryRepository;
        }

        @Override
        public CountryTestWrapperBase setIdOrSeed(long idOrSeed) {
            this.idOrSeed = idOrSeed;
            return this;
        }

        @Override
        public void build() {
            if (this.idOrSeed == null) throw new IllegalStateException("Not all required parameters are set");
            Country country = Country.builder().name("Country-" + this.idOrSeed).build();
            this.country = countryRepository.save(country);
        }

        @Override
        public long getId() {
            return this.country.getId();
        }

        @Override
        public Country get() {
            return this.country;
        }
    }
}
