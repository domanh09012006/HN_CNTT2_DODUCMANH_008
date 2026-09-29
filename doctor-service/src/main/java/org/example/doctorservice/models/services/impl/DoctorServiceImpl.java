package org.example.doctorservice.models.services.impl;

import lombok.RequiredArgsConstructor;
import org.example.doctorservice.exceptions.DoctorNotFoundException;
import org.example.doctorservice.models.entities.Doctor;
import org.example.doctorservice.models.repositories.DoctorRepository;
import org.example.doctorservice.models.services.DoctorService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {
    private final DoctorRepository doctorRepository;
    public DoctorServiceImpl(DoctorRepository roomRepository) {
        this.doctorRepository = roomRepository;
    }
    @Override
    @Cacheable(cacheNames = "doctors", key = "#id", sync = true)
    @Transactional(readOnly = true)
    public Doctor getDoctorById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException(id));
    }
}
