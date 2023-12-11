package healthy.lifestyle.backend.data.country;

import healthy.lifestyle.backend.users.model.Country;

public interface CountryTestWrapperBase {
    CountryTestWrapperBase setIdOrSeed(long idOrSeed);

    void build();

    long getId();

    Country get();
}
