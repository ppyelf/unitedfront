package com.iyundao.service.impl;

import com.iyundao.entity.Depart;
import com.iyundao.repository.DepartRepository;
import com.iyundao.service.DepartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @ClassName: DepartServiceImpl
 * @project: IYunDao
 * @author: 念
 * @Date: 2019/5/27 14:25
 * @Description: 实现 - 部门
 * @Version: V2.0
 */
@Service
@Transactional
public class DepartServiceImpl implements DepartService {

    @Autowired
    private DepartRepository departRepository;

    @Override
    public List<Depart> findBySubjectId(String subjectId) {
        return departRepository.findBySubjectId(subjectId);
    }

    @Override
    public List<Depart> getList() {
        return departRepository.getList();
    }

    @Override
    public Depart findById(String id) {
        return departRepository.findByDepartId(id);
    }

    @Override
    public Depart save(Depart depart) {
        return departRepository.save(depart);
    }

    @Override
    public List<Depart> findByIds(String[] departIds) {
        return departRepository.findByIds(departIds);
    }

    @Override
    public List<Depart>  saveAll(List<Depart> departs) {
        return departRepository.saveAll(departs);
    }

    @Override
    public List<Depart> findSubjectIsNull() {
        return departRepository.findSubjectIsNull();
    }

    @Override
    public List<Depart> findByFatherId(String id) {
        return departRepository.findByFatherId(id);
    }

    @Override
    public List<Depart> getListByFatherIdIsNull() {
        return departRepository.getListByFatherIdIsNull();
    }

    @Override
    public List<Depart> findBySubjectIdAndFatherIsNull(String subjectId) {
        return departRepository.findBySubjectIdAndFatherIsNull(subjectId);
    }

    @Override
    public boolean existsCode(String code) {
        Depart depart = departRepository.findByCode(code);
        return depart == null ? false : true;
    }

    @Override
    public Depart findByCode(String code) {
        return departRepository.findByCode(code);
    }

    @Override
    public List<Depart> findbyIds(String[] departIds) {
        return departRepository.findByIds(departIds);
    }

}
