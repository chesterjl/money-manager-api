package in.chester.moneymanager.service;

import in.chester.moneymanager.dto.ExpenseDTO;
import in.chester.moneymanager.dto.IncomeDTO;
import in.chester.moneymanager.entity.CategoryEntity;
import in.chester.moneymanager.entity.ExpenseEntity;
import in.chester.moneymanager.entity.IncomeEntity;
import in.chester.moneymanager.entity.ProfileEntity;
import in.chester.moneymanager.repository.CategoryRepository;
import in.chester.moneymanager.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncomeService {

    private final CategoryRepository categoryRepository;
    private final IncomeRepository incomeRepository;
    private final ProfileService profileService;

    public IncomeDTO addIncome(IncomeDTO incomeDTO) {
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findById(incomeDTO.getCategoryId()).orElseThrow(() -> new RuntimeException("Category not found"));
        IncomeEntity newIncome = convertToEntity(incomeDTO, profile, category);
        newIncome = incomeRepository.save(newIncome);
        return convertToDto(newIncome);
    }

    public List<IncomeDTO> getCurrentMonthIncomeForCurrentUser()  {
        ProfileEntity profile = profileService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        List<IncomeEntity> incomes = incomeRepository.findByProfileIdAndDateBetween(profile.getId(), startDate, endDate);
        return incomes.stream()
                .map(this::convertToDto)
                .toList();
    }

    public void deleteIncome(Long incomeId) {
        try {
            ProfileEntity profile = profileService.getCurrentProfile();
            IncomeEntity existingIncome = incomeRepository.findById(incomeId).orElseThrow(() -> new RuntimeException("Income not found"));

            if (!existingIncome.getProfile().getId().equals(profile.getId())){
                throw new RuntimeException("You are not authorized to delete this income");
            }
            incomeRepository.delete(existingIncome);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while deleted the income: " + e.getMessage());
        }
    }

    // Get latest 5 incomes for the current user
    public List<IncomeDTO> getLatest5IncomesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<IncomeEntity> incomes = incomeRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return incomes.stream()
                .map(this::convertToDto)
                .toList();
    }

    // Get total incomes for the current user
    public BigDecimal getTotalIncomesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        BigDecimal totalExpenses = incomeRepository.findTotalExpenseByProfileId(profile.getId());
        return totalExpenses != null ? totalExpenses : BigDecimal.ZERO;
    }

    // Filter incomes
    public List<IncomeDTO> filterIncomes(LocalDate startDate, LocalDate endDate, String keyword, Sort sort) {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<IncomeEntity> incomes = incomeRepository
                .findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(profile.getId(), startDate, endDate, keyword, sort);
        return incomes.stream()
                .map(this::convertToDto)
                .toList();
    }

    //helper methods
    private IncomeEntity convertToEntity(IncomeDTO incomeDTO, ProfileEntity profile, CategoryEntity category) {
        return IncomeEntity.builder()
                .name(incomeDTO.getName())
                .icon(incomeDTO.getIcon())
                .amount(incomeDTO.getAmount())
                .date(incomeDTO.getDate())
                .profile(profile)
                .category(category)
                .build();
    }

    //helper methods
    private IncomeDTO convertToDto(IncomeEntity incomeEntity) {
        return IncomeDTO.builder()
                .id(incomeEntity.getId())
                .name(incomeEntity.getName())
                .icon(incomeEntity.getIcon())
                .amount(incomeEntity.getAmount())
                .categoryId(incomeEntity.getCategory() != null ? incomeEntity.getCategory().getId() : null)
                .categoryName(incomeEntity.getCategory() != null ? incomeEntity.getCategory().getName() : "N/A")
                .date(incomeEntity.getDate())
                .createdAt(incomeEntity.getCreatedAt())
                .updatedAt(incomeEntity.getUpdatedAt())
                .build();
    }
}
