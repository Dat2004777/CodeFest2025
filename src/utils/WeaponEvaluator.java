package utils;

public interface WeaponEvaluator<T> {
    int evaluate(T weapon); // Bạn có thể để phase là null nếu chưa dùng
}

