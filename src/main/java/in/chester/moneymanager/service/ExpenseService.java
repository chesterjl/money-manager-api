package in.chester.moneymanager.service;

import in.chester.moneymanager.dto.ExpenseDTO;
import in.chester.moneymanager.entity.CategoryEntity;
import in.chester.moneymanager.entity.ExpenseEntity;
import in.chester.moneymanager.entity.ProfileEntity;
import in.chester.moneymanager.repository.CategoryRepository;
import in.chester.moneymanager.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final ProfileService profileService;

    public ExpenseDTO addExpense(ExpenseDTO expenseDTO) {
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findById(expenseDTO.getCategoryId()).orElseThrow(() -> new RuntimeException("Category not found"));
        ExpenseEntity newExpense = convertToEntity(expenseDTO, profile, category);
        newExpense = expenseRepository.save(newExpense);
        return convertToDto(newExpense);
    }

    public List<ExpenseDTO> getCurrentMonthExpensesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        List<ExpenseEntity> expenses = expenseRepository.findByProfileIdAndDateBetween(profile.getId(), startDate, endDate);
        return expenses.stream()
                .map(this::convertToDto)
                .toList();
    }

    public void deleteExpense(Long expenseId) {
        try {
            ProfileEntity profile = profileService.getCurrentProfile();
            ExpenseEntity existingExpense = expenseRepository.findById(expenseId).orElseThrow(() -> new RuntimeException("Expense not found"));

            if (!existingExpense.getProfile().getId().equals(profile.getId())){
                throw new RuntimeException("You are not authorized to delete this expense");
            }
            expenseRepository.delete(existingExpense);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while deleted the expense: " + e.getMessage());
        }
    }

    // Get latest 5 expenses for the current user
    public List<ExpenseDTO> getLatest5ExpensesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<ExpenseEntity> expenses = expenseRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return expenses.stream()
                .map(this::convertToDto)
                .toList();
    }

    // Get total expenses for the current user
    public BigDecimal getTotalExpensesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        BigDecimal totalExpenses = expenseRepository.findTotalExpenseByProfileId(profile.getId());
        return totalExpenses != null ? totalExpenses : BigDecimal.ZERO;
    }

    // Filter expenses
    public List<ExpenseDTO> filterExpenses(LocalDate startDate, LocalDate endDate, String keyword, Sort sort) {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<ExpenseEntity> expenses = expenseRepository
                .findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(profile.getId(), startDate, endDate, keyword, sort);
        return expenses.stream()
                .map(this::convertToDto)
                .toList();
    }

    // Notifications
    public List<ExpenseDTO> getExpensesForUserOnDate(Long profileId, LocalDate date) {
        List<ExpenseEntity> expenses = expenseRepository.findByProfileIdAndDate(profileId, date);
        return expenses.stream()
                .map(this::convertToDto)
                .toList();
    }

//    public ExpenseDTO updateExpense(Long expenseId, ExpenseDTO expenseDTO) {
//        ProfileEntity profile = profileService.getCurrentProfile();
//        ExpenseEntity existingExpense = expenseRepository.findById(expenseId).orElseThrow(() -> new RuntimeException("Expense not found"));
//
//        if (existingExpense.getProfile().getId().equals(profile.getId())){
//            throw new RuntimeException("You are not authorized to update this expense");
//        }
//        existingExpense.setName(expenseDTO.getName());
//        existingExpense.setAmount(expenseDTO.getAmount());
//        existingExpense.setIcon(expenseDTO.getIcon());
//        existingExpense.setDate(existingExpense.getDate());
//    }

    //helper methods
    private ExpenseEntity convertToEntity(ExpenseDTO expenseDTO, ProfileEntity profile, CategoryEntity category) {
        return ExpenseEntity.builder()
                .name(expenseDTO.getName())
                .icon(expenseDTO.getIcon())
                .amount(expenseDTO.getAmount())
                .date(expenseDTO.getDate())
                .profile(profile)
                .category(category)
                .build();
    }

    //helper methods
    private ExpenseDTO convertToDto(ExpenseEntity expenseEntity) {
        return ExpenseDTO.builder()
                .id(expenseEntity.getId())
                .name(expenseEntity.getName())
                .icon(expenseEntity.getIcon())
                .amount(expenseEntity.getAmount())
                .categoryId(expenseEntity.getCategory() != null ? expenseEntity.getCategory().getId() : null)
                .categoryName(expenseEntity.getCategory() != null ? expenseEntity.getCategory().getName() : "N/A")
                .date(expenseEntity.getDate())
                .createdAt(expenseEntity.getCreatedAt())
                .updatedAt(expenseEntity.getUpdatedAt())
                .build();
    }
}
