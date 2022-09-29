package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.lang.invoke.LambdaConversionException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        dishService.saveWithFlavor(dishDto);
        return R.success("添加菜品成功");

    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        Page<Dish> pageInfo = new Page(page, pageSize);
        Page<DishDto> dishDtoPage = new Page();
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(name != null, Dish::getName, name);
        lambdaQueryWrapper.orderByDesc(Dish::getUpdateTime);
        dishService.page(pageInfo);
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();
            Category byId = categoryService.getById(categoryId);
            String name1 = byId.getName();
            dishDto.setCategoryName(name1);
            return dishDto;
        }).collect(Collectors.toList());
        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);

    }

    @GetMapping("/{id}")
    public R<DishDto> getId(@PathVariable long id) {
        DishDto byIdWithFlavor = dishService.getByIdWithFlavor(id);
        return R.success(byIdWithFlavor);
    }

    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        dishService.saveWithFlavor(dishDto);
        return R.success("修改菜品成功");
    }
   /* @PostMapping("/status/0")
    public R<String> updateStatusStop(String ids){
        List<Long>id= Arrays.stream(ids.split(",")).map(item->Long.parseLong(item.trim())).collect(Collectors.toList());
        List<Dish> dishes = dishService.listByIds(id);
        for (Dish dish : dishes) {
            dish.setStatus(0);
            dishService.updateById(dish);
        }
        return R.success("更新成功");
    }
    @PostMapping("/status/1")
    public R<String> updateStatusStart(String ids){
        List<Long>id= Arrays.stream(ids.split(",")).map(item->Long.parseLong(item.trim())).collect(Collectors.toList());
        List<Dish> dishes = dishService.listByIds(id);
        for (Dish dish : dishes) {
            dish.setStatus(1);
            dishService.updateById(dish);
        }

        return R.success("更新成功");
    }*/

    /**
     * 根据id(批量)停售菜品信息
     *
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateMulStatus(@PathVariable Integer status, Long[] ids) {
        List<Long> list = Arrays.asList(ids);
        //list.forEach(System.out::println);

        //构造条件构造器
        LambdaUpdateWrapper<Dish> updateWrapper = new LambdaUpdateWrapper<>();
        //添加过滤条件
        updateWrapper.set(Dish::getStatus, status).in(Dish::getId, list);
        dishService.update(updateWrapper);

        return R.success("菜品信息修改成功");
    }

    /*   @DeleteMapping
       public  R<String> deleted(String ids){
           List<Long>id= Arrays.stream(ids.split(",")).map(item->Long.parseLong(item.trim())).collect(Collectors.toList());
           List<Dish> dishes = dishService.listByIds(id);
           for (Dish dish : dishes) {
               dishService.removeById(dish);
           }
           return R.success("删除成功");
       }*/
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        log.info("ids:{}", ids);

        dishService.removeWithFlavor(ids);
        return R.success("菜品信息删除成功");
    }

    @GetMapping("/list")
    public R<List<Dish>> list(Dish dish) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        queryWrapper.eq(Dish::getStatus,1);
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        queryWrapper.like(dish.getName() != null, Dish::getName, dish.getName());
        List<Dish> list = dishService.list(queryWrapper);
        return R.success(list);
    }
}
