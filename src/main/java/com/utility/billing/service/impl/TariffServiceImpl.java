package com.utility.billing.service.impl;

import com.utility.billing.dto.request.PenaltyRequest;
import com.utility.billing.dto.request.TariffRequest;
import com.utility.billing.dto.request.TaxRequest;
import com.utility.billing.dto.response.PenaltyResponse;
import com.utility.billing.dto.response.TariffResponse;
import com.utility.billing.dto.response.TaxResponse;
import com.utility.billing.entity.Penalty;
import com.utility.billing.entity.Tariff;
import com.utility.billing.entity.TariffTier;
import com.utility.billing.entity.Tax;
import com.utility.billing.entity.enums.TariffType;
import com.utility.billing.exception.BusinessRuleException;
import com.utility.billing.exception.ResourceNotFoundException;
import com.utility.billing.mapper.EntityMapper;
import com.utility.billing.repository.PenaltyRepository;
import com.utility.billing.repository.TariffRepository;
import com.utility.billing.repository.TaxRepository;
import com.utility.billing.service.TariffService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TariffServiceImpl implements TariffService {

    private final TariffRepository tariffRepository;
    private final TaxRepository taxRepository;
    private final PenaltyRepository penaltyRepository;

    // ---------------------------------------------------------------- Tariffs

    @Override
    @Transactional
    public TariffResponse createTariff(TariffRequest request) {
        validateTariffShape(request);

        // Versioning: close the current active tariff (same meter type) the day
        // before the new one starts, so old cycles keep the old tariff.
        var previous = tariffRepository.findTopByMeterTypeOrderByVersionDesc(request.meterType());
        int nextVersion = previous.map(t -> t.getVersion() + 1).orElse(1);
        previous.ifPresent(p -> {
            if (p.getEffectiveEnd() == null || p.getEffectiveEnd().isAfter(request.effectiveStart())) {
                if (!request.effectiveStart().isAfter(p.getEffectiveStart())) {
                    throw new BusinessRuleException(
                            "New tariff effective start must be after the current version's start ("
                                    + p.getEffectiveStart() + ")");
                }
                p.setEffectiveEnd(request.effectiveStart().minusDays(1));
                tariffRepository.save(p);
            }
        });

        Tariff tariff = Tariff.builder()
                .name(request.name())
                .meterType(request.meterType())
                .tariffType(request.tariffType())
                .version(nextVersion)
                .ratePerUnit(request.ratePerUnit())
                .serviceCharge(request.serviceCharge())
                .effectiveStart(request.effectiveStart())
                .effectiveEnd(null)
                .build();

        if (request.tariffType() == TariffType.TIERED) {
            for (var tierReq : request.tiers()) {
                tariff.getTiers().add(TariffTier.builder()
                        .tariff(tariff)
                        .upToUnit(tierReq.upToUnit())
                        .ratePerUnit(tierReq.ratePerUnit())
                        .build());
            }
        }

        return EntityMapper.toTariffResponse(tariffRepository.save(tariff));
    }

    private void validateTariffShape(TariffRequest request) {
        if (request.tariffType() == TariffType.FLAT) {
            if (request.ratePerUnit() == null) {
                throw new BusinessRuleException("FLAT tariff requires ratePerUnit");
            }
        } else { // TIERED
            if (request.tiers() == null || request.tiers().isEmpty()) {
                throw new BusinessRuleException("TIERED tariff requires at least one tier");
            }
        }
    }

    @Override
    public List<TariffResponse> getAllTariffs() {
        return tariffRepository.findAll().stream().map(EntityMapper::toTariffResponse).toList();
    }

    @Override
    public TariffResponse getTariff(Long id) {
        return EntityMapper.toTariffResponse(tariffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tariff", id)));
    }

    // ------------------------------------------------------------------- Taxes

    @Override
    @Transactional
    public TaxResponse createTax(TaxRequest request) {
        var previous = taxRepository.findTopByOrderByVersionDesc();
        int nextVersion = previous.map(t -> t.getVersion() + 1).orElse(1);
        previous.ifPresent(p -> {
            if (p.getEffectiveEnd() == null) {
                p.setEffectiveEnd(request.effectiveStart().minusDays(1));
                taxRepository.save(p);
            }
        });

        Tax tax = Tax.builder()
                .name(request.name())
                .percentage(request.percentage())
                .version(nextVersion)
                .effectiveStart(request.effectiveStart())
                .build();
        return EntityMapper.toTaxResponse(taxRepository.save(tax));
    }

    @Override
    public List<TaxResponse> getAllTaxes() {
        return taxRepository.findAll().stream().map(EntityMapper::toTaxResponse).toList();
    }

    // --------------------------------------------------------------- Penalties

    @Override
    @Transactional
    public PenaltyResponse createPenalty(PenaltyRequest request) {
        var previous = penaltyRepository.findTopByOrderByVersionDesc();
        int nextVersion = previous.map(p -> p.getVersion() + 1).orElse(1);
        previous.ifPresent(p -> {
            if (p.getEffectiveEnd() == null) {
                p.setEffectiveEnd(request.effectiveStart().minusDays(1));
                penaltyRepository.save(p);
            }
        });

        Penalty penalty = Penalty.builder()
                .name(request.name())
                .percentage(request.percentage())
                .version(nextVersion)
                .effectiveStart(request.effectiveStart())
                .build();
        return EntityMapper.toPenaltyResponse(penaltyRepository.save(penalty));
    }

    @Override
    public List<PenaltyResponse> getAllPenalties() {
        return penaltyRepository.findAll().stream().map(EntityMapper::toPenaltyResponse).toList();
    }
}
