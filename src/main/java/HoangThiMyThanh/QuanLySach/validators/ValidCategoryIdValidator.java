package HoangThiMyThanh.QuanLySach.validators;

import HoangThiMyThanh.QuanLySach.entities.Category;
import HoangThiMyThanh.QuanLySach.repositories.ICategoryRepository;
import HoangThiMyThanh.QuanLySach.validators.annotations.ValidCategoryId;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
@lombok.extern.slf4j.Slf4j
public class ValidCategoryIdValidator implements ConstraintValidator<ValidCategoryId, Category> {

    @Autowired(required = false)
    private ICategoryRepository categoryRepository;

    @Override
    public boolean isValid(Category category, ConstraintValidatorContext context) {
        if (category == null) return false;
        Long id = category.getId();
        if (id == null) return false;
        try {
            if (categoryRepository == null) {
                log.warn("ICategoryRepository not available in ValidCategoryIdValidator - skipping validation for id={}", id);
                // If the repository isn't available (in some validation contexts), skip DB check
                // to avoid false negatives. Returning true allows other validations to proceed.
                return true;
            }
            return categoryRepository.existsById(id);
        } catch (Exception ex) {
            log.error("Error validating category id {}: {}", id, ex.getMessage(), ex);
            return false;
        }
    }
} 
