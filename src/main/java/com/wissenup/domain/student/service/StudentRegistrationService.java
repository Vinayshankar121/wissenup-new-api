package com.wissenup.domain.student.service;

import com.wissenup.domain.academic.entity.AcademicYear;
import com.wissenup.domain.academic.entity.Section;
import com.wissenup.domain.academic.repository.AcademicYearRepository;
import com.wissenup.domain.academic.repository.ClassRepository;
import com.wissenup.domain.academic.repository.SectionRepository;
import com.wissenup.domain.student.dto.*;
import com.wissenup.domain.student.entity.*;
import com.wissenup.domain.student.repository.*;
import com.wissenup.domain.identity.service.ParentAccountService;
import com.wissenup.shared.exception.ConflictException;
import com.wissenup.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service @RequiredArgsConstructor @Transactional
public class StudentRegistrationService {
    private final StudentRepository students;
    private final ParentRepository parents;
    private final AddressRepository addresses;
    private final StudentParentRepository studentParents;
    private final StudentEnrollmentRepository enrollments;
    private final AcademicYearRepository academicYears;
    private final ClassRepository classes;
    private final SectionRepository sections;
    private final ParentAccountService parentAccounts;

    public StudentResponse register(StudentRegistrationRequest request, Long organizationId, Long userId) {
        if (students.existsByOrganizationIdAndAdmissionNo(organizationId, request.getAdmissionNo().trim()))
            throw ConflictException.duplicate("Student", "admission number", request.getAdmissionNo());

        validateAcademicPlacement(request.getEnrollment(), organizationId, null);
        Parent parent = parents.findByOrganizationIdAndPhoneNumber(organizationId, request.getParentPhoneNumber().trim())
            .orElseGet(() -> createParent(request.getParent(), organizationId, userId));
        if (parent.getUserId() == null) parentAccounts.provision(parent, request.getParent().getPassword(), userId);

        Student student = students.save(Student.builder()
            .organizationId(organizationId).admissionNo(request.getAdmissionNo().trim())
            .firstName(request.getFirstName().trim()).lastName(trim(request.getLastName()))
            .gender(request.getGender().trim().toUpperCase()).dateOfBirth(request.getDateOfBirth())
            .bloodGroup(trim(request.getBloodGroup())).admissionDate(request.getAdmissionDate())
            .status("ACTIVE").createdBy(userId).build());

        studentParents.save(StudentParent.builder().organizationId(organizationId).studentId(student.getStudentId())
            .parentId(parent.getParentId()).relationship(request.getRelationship().trim().toUpperCase()).isPrimary(true).build());

        var er = request.getEnrollment();
        StudentEnrollment enrollment = enrollments.save(StudentEnrollment.builder().organizationId(organizationId)
            .studentId(student.getStudentId()).academicYearId(er.getAcademicYearId()).classId(er.getClassId())
            .sectionId(er.getSectionId()).rollNo(trim(er.getRollNo())).enrollmentDate(er.getEnrollmentDate())
            .promotionStatus(trim(er.getPromotionStatus())).status("ACTIVE").build());

        Section section = sections.findById(er.getSectionId()).orElseThrow();
        section.setCurrentStrength((section.getCurrentStrength() == null ? 0 : section.getCurrentStrength()) + 1);
        sections.save(section);
        return response(student, enrollment, parent, request.getRelationship());
    }

    @Transactional(readOnly=true)
    public List<StudentResponse> list(Long organizationId) {
        return students.findAllByOrganizationIdOrderByCreatedAtDesc(organizationId).stream().map(this::responseFor).toList();
    }

    @Transactional(readOnly=true)
    public StudentResponse get(Long id, Long organizationId) {
        return responseFor(students.findByStudentIdAndOrganizationId(id, organizationId)
            .orElseThrow(() -> ResourceNotFoundException.notFound("Student", id)));
    }

    public StudentResponse update(Long id, StudentRegistrationRequest request, Long organizationId) {
        Student student = students.findByStudentIdAndOrganizationId(id, organizationId)
            .orElseThrow(() -> ResourceNotFoundException.notFound("Student", id));
        String admissionNo = request.getAdmissionNo().trim();
        students.findByOrganizationIdAndAdmissionNo(organizationId, admissionNo)
            .filter(other -> !other.getStudentId().equals(id))
            .ifPresent(other -> { throw ConflictException.duplicate("Student", "admission number", admissionNo); });

        StudentEnrollment enrollment = enrollments.findFirstByStudentIdOrderByEnrollmentIdDesc(id)
            .orElseThrow(() -> new ResourceNotFoundException("Enrollment for student " + id + " not found"));
        validateAcademicPlacement(request.getEnrollment(), organizationId, enrollment.getSectionId());
        student.setAdmissionNo(admissionNo);
        student.setFirstName(request.getFirstName().trim());
        student.setLastName(trim(request.getLastName()));
        student.setGender(request.getGender().trim().toUpperCase());
        student.setDateOfBirth(request.getDateOfBirth());
        student.setBloodGroup(trim(request.getBloodGroup()));
        student.setAdmissionDate(request.getAdmissionDate());
        students.save(student);

        var er = request.getEnrollment();
        if (!enrollment.getSectionId().equals(er.getSectionId())) {
            sections.findById(enrollment.getSectionId()).ifPresent(oldSection -> {
                oldSection.setCurrentStrength(Math.max(0, (oldSection.getCurrentStrength() == null ? 0 : oldSection.getCurrentStrength()) - 1));
                sections.save(oldSection);
            });
            Section newSection = sections.findById(er.getSectionId()).orElseThrow();
            newSection.setCurrentStrength((newSection.getCurrentStrength() == null ? 0 : newSection.getCurrentStrength()) + 1);
            sections.save(newSection);
        }
        enrollment.setAcademicYearId(er.getAcademicYearId());
        enrollment.setClassId(er.getClassId());
        enrollment.setSectionId(er.getSectionId());
        enrollment.setRollNo(trim(er.getRollNo()));
        enrollment.setEnrollmentDate(er.getEnrollmentDate());
        enrollment.setPromotionStatus(trim(er.getPromotionStatus()));
        enrollments.save(enrollment);

        StudentParent link = studentParents.findFirstByStudentIdOrderByIsPrimaryDesc(id)
            .orElseThrow(() -> new ResourceNotFoundException("Parent link for student " + id + " not found"));
        Parent parent = parents.findById(link.getParentId())
            .orElseThrow(() -> ResourceNotFoundException.notFound("Parent", link.getParentId()));
        var pd = request.getParent();
        parent.setFirstName(pd.getFirstName().trim());
        parent.setLastName(trim(pd.getLastName()));
        parent.setPhoneNumber(pd.getPhoneNumber().trim());
        parent.setEmail(emptyToNull(pd.getEmail()));
        parent.setOccupation(trim(pd.getOccupation()));
        if (parent.getAddressId() != null && pd.getAddress() != null) {
            addresses.findById(parent.getAddressId()).ifPresent(address -> {
                var a = pd.getAddress();
                address.setAddressLine1(trim(a.getAddressLine1())); address.setAddressLine2(trim(a.getAddressLine2()));
                address.setLocality(trim(a.getLocality())); address.setCity(trim(a.getCity()));
                address.setState(trim(a.getState())); address.setCountry(trim(a.getCountry())); address.setZipCode(trim(a.getZipCode()));
                addresses.save(address);
            });
        }
        parents.save(parent);
        link.setRelationship(request.getRelationship().trim().toUpperCase());
        studentParents.save(link);
        return response(student, enrollment, parent, link.getRelationship());
    }

    public void delete(Long id, Long organizationId) {
        Student student = students.findByStudentIdAndOrganizationId(id, organizationId)
            .orElseThrow(() -> ResourceNotFoundException.notFound("Student", id));
        enrollments.findFirstByStudentIdOrderByEnrollmentIdDesc(id).ifPresent(enrollment -> sections.findById(enrollment.getSectionId()).ifPresent(section -> {
            section.setCurrentStrength(Math.max(0, (section.getCurrentStrength() == null ? 0 : section.getCurrentStrength()) - 1));
            sections.save(section);
        }));
        studentParents.deleteAllByStudentId(id); enrollments.deleteAllByStudentId(id); students.delete(student);
    }

    @Transactional(readOnly=true)
    public Parent findParent(Long organizationId, String phone) {
        return parents.findByOrganizationIdAndPhoneNumber(organizationId, phone.trim()).orElse(null);
    }

    @Transactional(readOnly=true)
    public Parent findParentByUser(Long userId, Long organizationId) {
        return parents.findByUserIdAndOrganizationId(userId, organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Parent profile for user " + userId + " not found"));
    }

    @Transactional(readOnly=true)
    public List<StudentResponse> findChildren(Long parentId, Long organizationId) {
        Parent parent = parents.findById(parentId)
            .filter(row -> row.getOrganizationId().equals(organizationId))
            .orElseThrow(() -> ResourceNotFoundException.notFound("Parent", parentId));
        return studentParents.findAllByParentId(parent.getParentId()).stream()
            .map(link -> students.findByStudentIdAndOrganizationId(link.getStudentId(), organizationId)
                .map(student -> {
                    StudentEnrollment enrollment = enrollments.findFirstByStudentIdOrderByEnrollmentIdDesc(student.getStudentId()).orElse(null);
                    return response(student, enrollment, parent, link.getRelationship());
                }).orElse(null))
            .filter(Objects::nonNull)
            .toList();
    }

    @Transactional(readOnly=true)
    public List<StudentResponse> enrolledStudents(Long organizationId, Long classId, Long sectionId) {
        com.wissenup.domain.academic.entity.Class clazz=classes.findById(classId)
            .filter(row -> organizationId.equals(row.getOrganizationId()))
            .orElseThrow(() -> ResourceNotFoundException.notFound("Class", classId));
        sections.findById(sectionId)
            .filter(row -> organizationId.equals(row.getOrganizationId()) && classId.equals(row.getClassId()))
            .orElseThrow(() -> ResourceNotFoundException.notFound("Section", sectionId));
        return enrollments.findAllByOrganizationIdAndClassIdAndSectionIdAndStatusIgnoreCaseOrderByRollNoAsc(
                organizationId, clazz.getClassId(), sectionId, "ACTIVE").stream()
            .map(enrollment -> students.findByStudentIdAndOrganizationId(enrollment.getStudentId(), organizationId)
                .filter(student -> "ACTIVE".equalsIgnoreCase(student.getStatus()))
                .map(student -> response(student, enrollment, null, null)).orElse(null))
            .filter(Objects::nonNull)
            .toList();
    }

    private Parent createParent(StudentRegistrationRequest.ParentData data, Long organizationId, Long userId) {
        Long addressId = data.getAddressId();
        if (addressId == null && data.getAddress() != null) {
            var a=data.getAddress();
            addressId=addresses.save(Address.builder().organizationId(organizationId).addressLine1(trim(a.getAddressLine1()))
                .addressLine2(trim(a.getAddressLine2())).locality(trim(a.getLocality())).city(trim(a.getCity()))
                .state(trim(a.getState())).country(trim(a.getCountry())).zipCode(trim(a.getZipCode())).status("ACTIVE").build()).getAddressId();
        }
        return parents.save(Parent.builder().organizationId(organizationId).firstName(data.getFirstName().trim())
            .lastName(trim(data.getLastName())).phoneNumber(data.getPhoneNumber().trim()).email(emptyToNull(data.getEmail()))
            .occupation(trim(data.getOccupation())).addressId(addressId).status("ACTIVE").createdBy(userId).build());
    }

    private void validateAcademicPlacement(StudentRegistrationRequest.EnrollmentData e, Long orgId, Long currentSectionId) {
        AcademicYear year=academicYears.findById(e.getAcademicYearId()).orElseThrow(() -> ResourceNotFoundException.notFound("Academic year",e.getAcademicYearId()));
        com.wissenup.domain.academic.entity.Class clazz=classes.findById(e.getClassId()).orElseThrow(() -> ResourceNotFoundException.notFound("Class",e.getClassId()));
        Section section=sections.findById(e.getSectionId()).orElseThrow(() -> ResourceNotFoundException.notFound("Section",e.getSectionId()));
        if (!orgId.equals(year.getOrganizationId()) || !orgId.equals(clazz.getOrganizationId()) || !orgId.equals(section.getOrganizationId())
            || !e.getAcademicYearId().equals(clazz.getAcademicYearId()) || !e.getClassId().equals(section.getClassId()))
            throw new IllegalArgumentException("Academic year, class, and section do not belong to the same organization");
        if (!section.getSectionId().equals(currentSectionId) && section.getCapacity()!=null && section.getCurrentStrength()!=null && section.getCurrentStrength()>=section.getCapacity())
            throw new ConflictException("The selected section is full");
    }

    private StudentResponse responseFor(Student s) {
        StudentEnrollment e=enrollments.findFirstByStudentIdOrderByEnrollmentIdDesc(s.getStudentId()).orElse(null);
        StudentParent link=studentParents.findFirstByStudentIdOrderByIsPrimaryDesc(s.getStudentId()).orElse(null);
        Parent p=link==null?null:parents.findById(link.getParentId()).orElse(null);
        return response(s,e,p,link==null?null:link.getRelationship());
    }
    private StudentResponse response(Student s, StudentEnrollment e, Parent p, String relationship) {
        return StudentResponse.builder().studentId(s.getStudentId()).organizationId(s.getOrganizationId()).admissionNo(s.getAdmissionNo())
            .firstName(s.getFirstName()).lastName(s.getLastName()).gender(s.getGender()).dateOfBirth(s.getDateOfBirth())
            .bloodGroup(s.getBloodGroup()).admissionDate(s.getAdmissionDate()).status(s.getStatus())
            .academicYearId(e==null?null:e.getAcademicYearId()).classId(e==null?null:e.getClassId()).sectionId(e==null?null:e.getSectionId())
            .rollNo(e==null?null:e.getRollNo()).enrollmentDate(e==null?null:e.getEnrollmentDate())
            .parentId(p==null?null:p.getParentId()).parentName(p==null?null:(p.getFirstName()+" "+Optional.ofNullable(p.getLastName()).orElse("")).trim())
            .parentMobile(p==null?null:p.getPhoneNumber()).parentEmail(p==null?null:p.getEmail()).parentRelationship(relationship)
            .parentOccupation(p==null?null:p.getOccupation()).parentAddressId(p==null?null:p.getAddressId()).build();
    }
    private String trim(String s){return s==null?null:s.trim();}
    private String emptyToNull(String s){return s==null||s.isBlank()?null:s.trim();}
}
