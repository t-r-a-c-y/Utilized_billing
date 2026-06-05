package com.utility.billing.service;

import com.utility.billing.dto.request.PenaltyRequest;
import com.utility.billing.dto.request.TariffRequest;
import com.utility.billing.dto.request.TaxRequest;
import com.utility.billing.dto.response.PenaltyResponse;
import com.utility.billing.dto.response.TariffResponse;
import com.utility.billing.dto.response.TaxResponse;

import java.util.List;

/**
 * Configuration of versioned tariffs, taxes and penalties. Creating a new
 * version automatically closes the previously active version so it only applies
 * to billing cycles before the new effective start.
 */
public interface TariffService {

    TariffResponse createTariff(TariffRequest request);
    List<TariffResponse> getAllTariffs();
    TariffResponse getTariff(Long id);

    TaxResponse createTax(TaxRequest request);
    List<TaxResponse> getAllTaxes();

    PenaltyResponse createPenalty(PenaltyRequest request);
    List<PenaltyResponse> getAllPenalties();
}
