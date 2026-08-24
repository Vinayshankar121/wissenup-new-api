package com.wissenup.domain.staff.service;

import com.wissenup.domain.identity.entity.Role;
import com.wissenup.domain.identity.entity.User;
import com.wissenup.domain.identity.entity.UserRole;
import com.wissenup.domain.identity.repository.RoleRepository;
import com.wissenup.domain.identity.repository.UserRepository;
import com.wissenup.domain.identity.repository.UserRoleRepository;
import com.wissenup.domain.staff.dto.StaffRequest;
import com.wissenup.domain.staff.entity.Department;
import com.wissenup.domain.staff.entity.Designation;
import com.wissenup.domain.staff.entity.Staff;
import com.wissenup.domain.staff.entity.StaffClassAssignment;
import com.wissenup.domain.staff.entity.StaffSubjectAssignment;
import com.wissenup.domain.staff.repository.DepartmentRepository;
import com.wissenup.domain.staff.repository.DesignationRepository;
import com.wissenup.domain.staff.repository.StaffRepository;
import com.wissenup.domain.staff.repository.StaffClassAssignmentRepository;
import com.wissenup.domain.staff.repository.StaffSubjectAssignmentRepository;
import com.wissenup.domain.student.entity.Address;
import com.wissenup.domain.student.repository.AddressRepository;
import com.wissenup.domain.platform.service.OnboardingEmailService;
import com.wissenup.shared.exception.ConflictException;
import com.wissenup.shared.exception.ResourceNotFoundException;
import com.wissenup.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.security.SecureRandom;
@Service @RequiredArgsConstructor @Transactional
public class StaffManagementService {
 private final DepartmentRepository departments; private final DesignationRepository designations; private final StaffRepository staff;
 private final AddressRepository addresses; private final UserRepository users; private final UserRoleRepository userRoles; private final RoleRepository roles; private final PasswordEncoder encoder;
 private final OnboardingEmailService credentialEmailService;
 private final StaffClassAssignmentRepository classAssignments;
 private final StaffSubjectAssignmentRepository subjectAssignments;
 private static final SecureRandom RANDOM = new SecureRandom();
 private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
 private static final String LOWER = "abcdefghijkmnopqrstuvwxyz";
 private static final String DIGITS = "23456789";
 private static final String SYMBOLS = "@#$%&*!";
 @Transactional(readOnly=true) public List<Department> departments(Long org){return departments.findAllByOrganizationIdOrderByName(org);}
 @Transactional(readOnly=true) public List<Designation> designations(Long org){return designations.findAllByOrganizationIdOrderByName(org);}
 @Transactional(readOnly=true) public List<Staff> staff(Long org){return staff.findAllByOrganizationIdOrderByCreatedAtDesc(org);}
 @Transactional(readOnly=true) public Staff get(Long id,Long org){return staff.findByStaffIdAndOrganizationId(id,org).orElseThrow(()->ResourceNotFoundException.notFound("Staff",id));}
 public Department saveDepartment(Department d,Long org,Long user){if(d.getName()==null||d.getName().isBlank())throw new ValidationException("Department name is required");d.setName(d.getName().trim());d.setOrganizationId(org);d.setCreatedBy(user);d.setStatus(d.getStatus()==null?"ACTIVE":normalizeStatus(d.getStatus()));return departments.save(d);}
 public Department updateDepartment(Long id,Department value,Long org){
  Department current=departments.findByDepartmentIdAndOrganizationId(id,org).orElseThrow(()->ResourceNotFoundException.notFound("Department",id));
  if(value.getName()==null||value.getName().isBlank())throw new ValidationException("Department name is required");
  current.setName(value.getName().trim()); current.setDepartmentType(value.getDepartmentType());
  if(value.getStatus()!=null)current.setStatus(normalizeStatus(value.getStatus()));
  return departments.save(current);
 }
 public Department departmentStatus(Long id,Long org,String status){
  Department current=departments.findByDepartmentIdAndOrganizationId(id,org).orElseThrow(()->ResourceNotFoundException.notFound("Department",id));
  current.setStatus(normalizeStatus(status));
  if("INACTIVE".equals(current.getStatus()))designations.findAllByOrganizationIdOrderByName(org).stream().filter(d->id.equals(d.getDepartmentId())).forEach(d->d.setStatus("INACTIVE"));
  return departments.save(current);
 }
 public Designation saveDesignation(Designation d,Long org,Long user){if(d.getDepartmentId()==null)throw new ValidationException("Department is required");if(d.getName()==null||d.getName().isBlank())throw new ValidationException("Designation name is required");departments.findByDepartmentIdAndOrganizationId(d.getDepartmentId(),org).orElseThrow(()->ResourceNotFoundException.notFound("Department",d.getDepartmentId()));d.setName(d.getName().trim());d.setOrganizationId(org);d.setCreatedBy(user);d.setStatus(d.getStatus()==null?"ACTIVE":normalizeStatus(d.getStatus()));return designations.save(d);}
 public Designation updateDesignation(Long id,Designation value,Long org){
  Designation current=designations.findByDesignationIdAndOrganizationId(id,org).orElseThrow(()->ResourceNotFoundException.notFound("Designation",id));
  if(value.getDepartmentId()==null)throw new ValidationException("Department is required");
  departments.findByDepartmentIdAndOrganizationId(value.getDepartmentId(),org).orElseThrow(()->ResourceNotFoundException.notFound("Department",value.getDepartmentId()));
  if(value.getName()==null||value.getName().isBlank())throw new ValidationException("Designation name is required");
  current.setDepartmentId(value.getDepartmentId()); current.setName(value.getName().trim()); current.setDescription(value.getDescription());
  if(value.getStatus()!=null)current.setStatus(normalizeStatus(value.getStatus()));
  return designations.save(current);
 }
 public Designation designationStatus(Long id,Long org,String status){Designation current=designations.findByDesignationIdAndOrganizationId(id,org).orElseThrow(()->ResourceNotFoundException.notFound("Designation",id));current.setStatus(normalizeStatus(status));return designations.save(current);}
 public Staff create(StaffRequest r,Long org,Long createdBy){
  if(staff.existsByOrganizationIdAndEmployeeCode(org,r.getEmployeeCode().trim()))throw ConflictException.duplicate("Staff","employee code",r.getEmployeeCode());
  departments.findByDepartmentIdAndOrganizationId(r.getDepartmentId(),org).orElseThrow(()->ResourceNotFoundException.notFound("Department",r.getDepartmentId()));
  designations.findByDesignationIdAndOrganizationId(r.getDesignationId(),org).orElseThrow(()->ResourceNotFoundException.notFound("Designation",r.getDesignationId()));
  String email=r.getEmail().trim().toLowerCase(); if(users.existsByEmail(email))throw ConflictException.duplicate("User","email",email);
  String temporaryPassword=generateTemporaryPassword();
  User user=users.save(User.builder().organizationId(org).email(email).phoneNumber(r.getPhoneNumber().trim()).password(encoder.encode(temporaryPassword)).status("ACTIVE").createdBy(createdBy).build());
  Role role=roles.findByCode(r.getRoleName().trim().toUpperCase()).orElseThrow(()->new ResourceNotFoundException("Role",r.getRoleName()));
  userRoles.save(UserRole.builder().userId(user.getUserId()).roleId(role.getRoleId()).createdBy(createdBy).status("ACTIVE").build());
  Long addressId=null; if(r.getAddress()!=null){var a=r.getAddress();addressId=addresses.save(Address.builder().organizationId(org).addressLine1(a.getAddressLine1()).addressLine2(a.getAddressLine2()).locality(a.getLocality()).city(a.getCity()).state(a.getState()).country(a.getCountry()).zipCode(a.getZipCode()).status("ACTIVE").build()).getAddressId();}
  Staff saved=staff.save(Staff.builder().organizationId(org).userId(user.getUserId()).departmentId(r.getDepartmentId()).designationId(r.getDesignationId()).addressId(addressId).employeeCode(r.getEmployeeCode().trim()).firstName(r.getFirstName().trim()).lastName(r.getLastName()).gender(r.getGender()).dateOfBirth(r.getDateOfBirth()).joiningDate(r.getJoiningDate()).qualification(r.getQualification()).experience(r.getExperience()).email(email).phoneNumber(r.getPhoneNumber().trim()).roleName(role.getCode()).status(r.getStatus()==null?"ACTIVE":r.getStatus()).createdBy(createdBy).build());
  saveAssignments(r, saved.getStaffId(), org, createdBy);
  credentialEmailService.sendStaffCredentials(email, (r.getFirstName()+" "+(r.getLastName()==null?"":r.getLastName())).trim(), role.getName(), temporaryPassword);
  return saved;
 }
 private String generateTemporaryPassword(){
  char[] value=new char[14]; value[0]=pick(UPPER); value[1]=pick(LOWER); value[2]=pick(DIGITS); value[3]=pick(SYMBOLS);
  String all=UPPER+LOWER+DIGITS+SYMBOLS; for(int i=4;i<value.length;i++)value[i]=pick(all);
  for(int i=value.length-1;i>0;i--){int j=RANDOM.nextInt(i+1);char t=value[i];value[i]=value[j];value[j]=t;}
  return new String(value);
 }
 private char pick(String chars){return chars.charAt(RANDOM.nextInt(chars.length()));}
 private void saveAssignments(StaffRequest r,Long staffId,Long org,Long createdBy){
  var classAssignment=r.getClassTeacherAssignment();
  if(classAssignment!=null){validateAssignment(classAssignment,false);classAssignments.save(StaffClassAssignment.builder().organizationId(org).staffId(staffId).academicYearId(classAssignment.getAcademicYearId()).classId(classAssignment.getClassId()).sectionId(classAssignment.getSectionId()).status("ACTIVE").createdBy(createdBy).build());}
  if(r.getSubjectAssignments()!=null)for(var assignment:r.getSubjectAssignments()){validateAssignment(assignment,true);subjectAssignments.save(StaffSubjectAssignment.builder().organizationId(org).staffId(staffId).academicYearId(assignment.getAcademicYearId()).classId(assignment.getClassId()).sectionId(assignment.getSectionId()).subjectId(assignment.getSubjectId()).status("ACTIVE").createdBy(createdBy).build());}
 }
 private void validateAssignment(StaffRequest.AssignmentData a,boolean subjectRequired){if(a.getAcademicYearId()==null||a.getClassId()==null||a.getSectionId()==null||(subjectRequired&&a.getSubjectId()==null))throw new ValidationException("Academic year, class, section"+(subjectRequired?", and subject":"")+" are required for staff assignments");}
 @Transactional(readOnly=true) public List<StaffClassAssignment> classAssignments(Long id,Long org){get(id,org);return classAssignments.findAllByOrganizationIdAndStaffIdOrderByCreatedAtDesc(org,id);}
 @Transactional(readOnly=true) public List<StaffSubjectAssignment> subjectAssignments(Long id,Long org){get(id,org);return subjectAssignments.findAllByOrganizationIdAndStaffIdOrderByCreatedAtDesc(org,id);}
 public Staff update(Long id,StaffRequest r,Long org,Long updatedBy){
  Staff current=get(id,org);
  departments.findByDepartmentIdAndOrganizationId(r.getDepartmentId(),org).orElseThrow(()->ResourceNotFoundException.notFound("Department",r.getDepartmentId()));
  designations.findByDesignationIdAndOrganizationId(r.getDesignationId(),org).orElseThrow(()->ResourceNotFoundException.notFound("Designation",r.getDesignationId()));
  current.setEmployeeCode(r.getEmployeeCode().trim()); current.setFirstName(r.getFirstName().trim()); current.setLastName(r.getLastName());
  current.setGender(r.getGender()); current.setDateOfBirth(r.getDateOfBirth()); current.setJoiningDate(r.getJoiningDate());
  current.setDepartmentId(r.getDepartmentId()); current.setDesignationId(r.getDesignationId()); current.setQualification(r.getQualification());
  current.setExperience(r.getExperience()); current.setEmail(r.getEmail().trim().toLowerCase()); current.setPhoneNumber(r.getPhoneNumber().trim());
  if(r.getStatus()!=null)current.setStatus(r.getStatus().toUpperCase());
  Staff saved=staff.save(current);
  classAssignments.deleteAllByOrganizationIdAndStaffId(org,id);
  subjectAssignments.deleteAllByOrganizationIdAndStaffId(org,id);
  saveAssignments(r,id,org,updatedBy);
  return saved;
 }
 public void delete(Long id,Long org){status(id,org,"INACTIVE");}
 public Staff status(Long id,Long org,String value){Staff s=get(id,org);s.setStatus(normalizeStatus(value));users.findById(s.getUserId()).ifPresent(u->{u.setStatus(s.getStatus());users.save(u);});return staff.save(s);}
 private String normalizeStatus(String value){String status=value==null?"":value.trim().toUpperCase();if(!"ACTIVE".equals(status)&&!"INACTIVE".equals(status))throw new ValidationException("Status must be ACTIVE or INACTIVE");return status;}
}
