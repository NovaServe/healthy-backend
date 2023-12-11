package healthy.lifestyle.backend.data.country;

import healthy.lifestyle.backend.users.model.Country;

public class CountryTestBuilder {
    public CountryTestWrapper getWrapper() {
        return new CountryTestWrapper();
    }

    public static class CountryTestWrapper implements CountryTestWrapperBase {
        private Long idOrSeed;

        private Country country;

        @Override
        public CountryTestWrapperBase setIdOrSeed(long idOrSeed) {
            this.idOrSeed = idOrSeed;
            return this;
        }

        @Override
        public void build() {
            if (this.idOrSeed == null) throw new IllegalStateException("Not all required parameters are set");
            this.country = Country.builder()
                    .id(this.idOrSeed)
                    .name("Country-" + this.idOrSeed)
                    .build();
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
