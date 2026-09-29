package org.example.appointmentservice.models.services.impl;

import lombok.RequiredArgsConstructor;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.example.appointmentservice.models.constants.AppointmentStatus;
import org.example.appointmentservice.models.dto.requests.CreateAppointmentDetailRequest;
import org.example.appointmentservice.models.dto.requests.CreateAppointmentRequest;
import org.example.appointmentservice.models.dto.responses.AppointmentResponse;
import org.example.appointmentservice.models.dto.responses.DoctorResponse;
import org.example.appointmentservice.models.entities.Appointment;
import org.example.appointmentservice.models.entities.AppointmentDetail;
import org.example.appointmentservice.models.repositories.AppointmentDetailRepository;
import org.example.appointmentservice.models.repositories.AppointmentRepository;
import org.example.appointmentservice.models.services.AppointmentService;
import org.example.appointmentservice.models.dto.responses.AppointmentDetailResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final AppointmentDetailRepository appointmentDetailRepository;
    private final DoctorGatewayService doctorGatewayService;

    private ResolvedItem resolveDoctor(CreateAppointmentDetailRequest item) {
        DoctorResponse room = doctorGatewayService.getDoctorById(item.doctorId());
        return new ResolvedItem(item, room);
    }

    private record ResolvedItem(CreateAppointmentDetailRequest request, DoctorResponse room) {
    }
    @Override
    public AppointmentResponse createAppointment(CreateAppointmentRequest request) {
        List<ResolvedItem> resolveDoctor = request.items().stream()
                .map(this::resolveDoctor)
                .toList();

        double total = resolveDoctor.stream()
                .mapToDouble(item -> item.room().consultationFee() * item.request().slots())
                .sum();

        Appointment appointment = appointmentRepository.save(Appointment.builder()
                .patientName(request.patientName().trim())
                .patientEmail(request.patientEmail().trim())
                .totalAmount(total)
                .status(AppointmentStatus.PENDING)
                .build());
        List<AppointmentDetail> details = resolveDoctor.stream()
                .map(item -> AppointmentDetail.builder()
                        .appointment(appointment)
                        .slots(item.request().slots())
                        .consultationFee(item.room().consultationFee())
                        .doctorId(item.room().id())
                        .build())
                .toList();
        List<AppointmentDetail> savedDetails = appointmentDetailRepository.saveAll(details);
        List<AppointmentDetailResponse> itemResponses = getReservationDetailResponses(savedDetails, resolveDoctor);

        return new AppointmentResponse(
                appointment.getId(),
                appointment.getPatientName(),
                appointment.getPatientEmail(),
                appointment.getTotalAmount(),
                appointment.getStatus(),
                itemResponses);
    }
    private static @NonNull List<AppointmentDetailResponse> getReservationDetailResponses(List<AppointmentDetail> savedDetails,
                                                                                          List<ResolvedItem> resolvedItems) {
        List<AppointmentDetailResponse> itemResponses = new ArrayList<>(savedDetails.size());

        for (int index = 0; index < savedDetails.size(); index++) {
            AppointmentDetail detail = savedDetails.get(index);
            DoctorResponse room = resolvedItems.get(index).room();
            itemResponses.add(new AppointmentDetailResponse(
                    detail.getId(),
                    detail.getDoctorId(),
                    room.availableSlots(),
                    detail.getConsultationFee(),
                    detail.getConsultationFee() * detail.getSlots()));
        }
        return itemResponses;
    }
}
