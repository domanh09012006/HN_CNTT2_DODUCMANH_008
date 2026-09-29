package org.example.appointmentservice.models.services.impl;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.example.appointmentservice.clients.DoctorClient;
import org.example.appointmentservice.exceptions.DoctorNotFoundException;
import org.example.appointmentservice.exceptions.DoctorServiceException;
import org.example.appointmentservice.models.dto.responses.DoctorResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DoctorGatewayService {
    private final DoctorClient doctorClient;

    @CircuitBreaker(name = "doctorService", fallbackMethod = "getDoctorFallback")
    public DoctorResponse getDoctorById(Long doctorId) {
        DoctorResponse doctor = doctorClient.getDoctorById(doctorId);

        if (doctor == null || doctor.id() == null || doctor.consultationFee() == null) {
            throw new DoctorServiceException(
                    "Ko tìm thấy bác sĩ" + doctorId
            );
        }
        if (doctor.consultationFee() < 0) {
            throw new DoctorServiceException(
                    "Ko tìm thấy bác sĩ" + doctorId
            );
        }
        return doctor;
    }
    private DoctorResponse getDoctorFallback(Long doctorId, Throwable throwable) {
        if (throwable instanceof FeignException.NotFound) {
            throw new DoctorNotFoundException(doctorId);
        }
        if (throwable instanceof DoctorNotFoundException doctorNotFoundException) {
            throw doctorNotFoundException;
        }
        if (throwable instanceof DoctorServiceException doctorServiceException) {
            throw doctorServiceException;
        }

        throw new DoctorServiceException(
                "Ko tìm thấy nghiệp vụ bác sĩ" + doctorId,
                throwable
        );
    }

}
