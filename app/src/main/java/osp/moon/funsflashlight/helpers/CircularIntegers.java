package osp.moon.funsflashlight.helpers;

import android.content.Context;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import osp.moon.funsflashlight.customobjects.SolidColor;
import osp.moon.funsflashlight.database.AppDatabase;

public class CircularIntegers {
    private List<Integer> mIds;
    private int currentIndex = -1;
    private Random random = new Random();

    public CircularIntegers(final Context context, List<Integer> ids) {
        this.mIds = new ArrayList<>();
        for (Integer id : ids) {
            SolidColor solidColor = (SolidColor) AppDatabase.getSolidColor(context, id);
            this.mIds.add(solidColor.getColor());
        }
    }

    public Integer getNext() {
        if (this.mIds.isEmpty()) {
            int red = random.nextInt(256);
            int green = random.nextInt(256);
            int blue = random.nextInt(256);
            return Color.rgb(red, green, blue);
        }
        currentIndex++;
        if (currentIndex >= this.mIds.size()) {
            currentIndex = 0; // Возвращаемся к началу списка
        }
        return this.mIds.get(currentIndex);
    }

    // Дополнительно: сбросить на начало
    public void reset() {
        currentIndex = -1;
    }

    // Дополнительно: получить текущий элемент без смещения
    public Integer getCurrent() {
        if (currentIndex < 0 || this.mIds.isEmpty()) {
            // или вернуть null, или бросить исключение, в зависимости от логики
            return null;
        }
        return this.mIds.get(currentIndex);
    }

    // Дополнительно: установить новый список IDs
    public void setIds(List<Integer> newIds) {
        if (newIds == null || newIds.isEmpty()) {
            throw new IllegalArgumentException("Новый список ID не может быть null или пустым.");
        }
        this.mIds = newIds;
        reset(); // Сбрасываем индекс при смене списка
    }
}

