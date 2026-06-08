package com.bookstore.user.service;  // 声明当前类所在的包路径，这里是服务层

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;  // 导入MyBatis-Plus的Lambda查询构造器，用于构建类型安全的查询条件
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;  // 导入MyBatis-Plus的分页对象，用于分页查询
import com.bookstore.common.api.dto.LoginDTO;  // 导入登录数据传输对象，包含用户名和密码
import com.bookstore.common.api.dto.PasswordUpdateDTO;  // 导入密码修改数据传输对象，包含旧密码和新密码
import com.bookstore.common.api.dto.RegisterDTO;  // 导入注册数据传输对象，包含用户名、密码、邮箱、手机号
import com.bookstore.common.api.vo.PageResult;  // 导入分页结果封装类，用于返回分页数据
import com.bookstore.common.api.vo.UserVO;  // 导入用户视图对象（VO），用于返回给前端的用户数据
import com.bookstore.common.entity.Account;  // 导入用户账户实体类，对应数据库中的用户表
import com.bookstore.common.security.JwtUtil;  // 导入JWT工具类，用于生成和解析JWT Token
import com.bookstore.common.util.PasswordUtil;  // 导入密码工具类，用于密码加密和校验
import com.bookstore.user.mapper.AccountMapper;  // 导入用户账户Mapper接口，用于数据库操作
import lombok.RequiredArgsConstructor;  // 导入Lombok注解，自动生成包含final字段的构造函数
import org.springframework.beans.BeanUtils;  // 导入Spring的Bean工具类，用于对象属性的复制
import org.springframework.data.redis.core.RedisTemplate;  // 导入Redis操作模板，用于操作Redis缓存（如存储Token等）
import org.springframework.stereotype.Service;  // 导入Service注解，标记该类为Spring的服务层组件
import org.springframework.transaction.annotation.Transactional;  // 导入事务注解，用于声明方法在数据库事务中执行

import java.util.HashMap;  // 导入HashMap集合类，用于存储键值对数据
import java.util.List;  // 导入List集合接口
import java.util.Map;  // 导入Map集合接口
import java.util.stream.Collectors;  // 导入Stream的Collectors工具，用于将Stream流收集为集合

/**
 * 用户账户服务
 * 处理用户相关的所有业务逻辑，包括登录、注册、查询用户信息、修改密码、修改资料、
 * 管理员查询用户列表、修改用户状态、删除用户等。
 * <p>
 * 注解说明：
 * - @Service：标记为Spring的服务层组件，Spring会自动创建该类的实例（Bean）并管理其生命周期
 * - @RequiredArgsConstructor：Lombok注解，自动生成包含所有final字段的构造函数，实现依赖注入
 */
@Service  // 服务层注解，Spring会自动将该类注册为一个Bean，可以在控制器等地方注入使用
@RequiredArgsConstructor  // Lombok注解，自动生成包含final成员变量的构造函数，用于Spring的构造函数依赖注入
public class AccountService {

    private final AccountMapper accountMapper;  // 用户账户Mapper，用于对数据库中的用户表进行CRUD操作
    private final JwtUtil jwtUtil;  // JWT工具类，用于生成和解析Token，实现无状态的用户认证
    private final RedisTemplate<String, Object> redisTemplate;  // Redis操作模板，用于缓存数据（如Token黑名单等）

    /**
     * 用户登录方法
     * 验证用户名和密码，如果验证通过则生成JWT Token返回。
     *
     * @param dto 登录信息对象，包含用户名和密码
     * @return Map对象，包含生成的token和用户信息
     * @throws IllegalArgumentException 当用户名不存在、密码错误或账号被禁用时抛出异常
     */
    public Map<String, Object> login(LoginDTO dto) {  // 登录方法，接收登录DTO，返回包含token和用户信息的Map
        // 根据用户名（userid）从数据库查询用户信息
        Account account = accountMapper.selectOne(
                new LambdaQueryWrapper<Account>().eq(Account::getUserid, dto.getUsername()));  // 构建查询条件：userid = 传入的用户名
        // 校验用户是否存在，以及密码是否匹配（PasswordUtil.matches会将明文密码与加密后的密码进行比对）
        if (account == null || !PasswordUtil.matches(dto.getPassword(), account.getPassword())) {  // 用户不存在或密码不匹配
            throw new IllegalArgumentException("用户名或密码错误");  // 抛出异常，提示用户名或密码错误
        }
        // 检查账号是否被禁用（status=0 表示禁用，status=1 表示正常）
        if (account.getStatus() == 0) {  // 账号状态为0，表示已被禁用
            throw new IllegalArgumentException("账号已被禁用");  // 抛出异常，提示账号已被禁用
        }
        // 登录验证通过，生成JWT Token（传入用户ID和角色信息）
        String token = jwtUtil.generateToken(0L, account.getUserid(), account.getRole());  // 生成JWT Token，包含用户ID和角色
        // 创建结果Map，存放token和用户信息
        Map<String, Object> result = new HashMap<>();  // 创建一个HashMap用于存放返回结果
        result.put("token", token);  // 将生成的Token放入结果Map
        result.put("user", convertToVO(account));  // 将用户实体转换为视图对象（脱敏后）放入结果Map
        return result;  // 返回包含token和用户信息的Map
    }

    /**
     * 用户注册方法
     * 创建新用户账号。会先检查用户名和邮箱是否已被占用。
     *
     * @param dto 注册信息对象，包含用户名、密码、邮箱、手机号
     * @throws IllegalArgumentException 当用户名已存在或邮箱已被注册时抛出异常
     */
    @Transactional  // 事务注解，确保该方法在数据库事务中执行，出现异常时会自动回滚
    public void register(RegisterDTO dto) {  // 注册方法，接收注册DTO
        // 检查用户名是否已被占用
        if (accountMapper.selectOne(
                new LambdaQueryWrapper<Account>().eq(Account::getUserid, dto.getUsername())) != null) {  // 查询数据库中是否已存在该用户名
            throw new IllegalArgumentException("用户名已存在");  // 用户名已存在，抛出异常
        }
        // 检查邮箱是否已被注册
        if (accountMapper.selectOne(
                new LambdaQueryWrapper<Account>().eq(Account::getEmail, dto.getEmail())) != null) {  // 查询数据库中是否已存在该邮箱
            throw new IllegalArgumentException("邮箱已被注册");  // 邮箱已被注册，抛出异常
        }
        // 创建新的用户账户实体对象
        Account account = new Account();  // 实例化一个新的Account对象
        account.setUserid(dto.getUsername());  // 设置用户ID（即登录用的用户名）
        account.setPassword(PasswordUtil.encode(dto.getPassword()));  // 将明文密码加密后存储（使用BCrypt等加密算法）
        account.setEmail(dto.getEmail());  // 设置邮箱
        account.setPhone(dto.getPhone());  // 设置手机号
        account.setRole("user");  // 设置角色为普通用户（"user"）
        account.setStatus(1);  // 设置账号状态为启用（1表示正常）
        accountMapper.insert(account);  // 将新用户插入数据库
    }

    /**
     * 根据用户ID查询用户信息
     *
     * @param id 用户ID
     * @return 用户视图对象（UserVO），不包含密码等敏感信息
     * @throws IllegalArgumentException 当用户不存在时抛出异常
     */
    public UserVO getUserById(Long id) {  // 查询用户信息方法，接收用户ID
        // 根据ID从数据库查询用户，将Long类型的id转为String（因为数据库中userid是字符串类型）
        Account account = accountMapper.selectById(String.valueOf(id));  // 将Long类型的id转为String后查询数据库
        if (account == null) {  // 查询结果为空，说明用户不存在
            throw new IllegalArgumentException("用户不存在");  // 抛出异常，提示用户不存在
        }
        return convertToVO(account);  // 将用户实体转换为视图对象后返回
    }

    /**
     * 修改用户密码
     * 先验证旧密码是否正确，正确后更新为新密码。
     *
     * @param userId 用户ID
     * @param dto    密码修改对象，包含旧密码和新密码
     * @throws IllegalArgumentException 当用户不存在或旧密码错误时抛出异常
     */
    @Transactional  // 事务注解，确保密码修改操作的原子性
    public void updatePassword(Long userId, PasswordUpdateDTO dto) {  // 修改密码方法，接收用户ID和密码修改DTO
        // 根据ID查询用户
        Account account = accountMapper.selectById(String.valueOf(userId));  // 将Long类型的userId转为String后查询数据库
        if (account == null) {  // 查询结果为空
            throw new IllegalArgumentException("用户不存在");  // 抛出异常，提示用户不存在
        }
        // 验证旧密码是否正确
        if (!PasswordUtil.matches(dto.getOldPassword(), account.getPassword())) {  // 将用户输入的旧密码与数据库中加密的密码进行比对
            throw new IllegalArgumentException("原密码错误");  // 旧密码不匹配，抛出异常
        }
        // 旧密码验证通过，将新密码加密后更新到数据库
        account.setPassword(PasswordUtil.encode(dto.getNewPassword()));  // 对新密码进行加密处理
        accountMapper.updateById(account);  // 根据主键更新用户记录
    }

    /**
     * 修改用户资料
     * 修改用户的邮箱、手机号、头像等个人信息。
     *
     * @param userId 用户ID
     * @param vo     用户视图对象，包含要更新的资料信息
     * @throws IllegalArgumentException 当用户不存在时抛出异常
     */
    @Transactional  // 事务注解，确保资料修改操作的原子性
    public void updateProfile(Long userId, UserVO vo) {  // 修改资料方法，接收用户ID和包含新资料的UserVO
        // 根据ID查询用户
        Account account = accountMapper.selectById(String.valueOf(userId));  // 将Long类型的userId转为String后查询数据库
        if (account == null) {  // 查询结果为空
            throw new IllegalArgumentException("用户不存在");  // 抛出异常，提示用户不存在
        }
        account.setEmail(vo.getEmail());  // 更新邮箱
        account.setPhone(vo.getPhone());  // 更新手机号
        account.setAvatar(vo.getAvatar());  // 更新头像URL
        accountMapper.updateById(account);  // 根据主键更新用户记录
    }

    /**
     * 分页查询用户列表（管理员功能）
     * 支持按关键词模糊搜索用户名、邮箱或手机号。
     *
     * @param pageNum  当前页码
     * @param pageSize 每页显示条数
     * @param keyword  搜索关键词（可为null）
     * @return 分页结果对象，包含用户列表和分页信息
     */
    public PageResult<UserVO> getUserList(Integer pageNum, Integer pageSize, String keyword) {  // 分页查询用户列表方法
        // 创建MyBatis-Plus的分页对象，指定当前页和每页大小
        Page<Account> page = new Page<>(pageNum, pageSize);  // 创建分页对象，pageNum是页码，pageSize是每页条数
        // 创建查询条件构造器
        LambdaQueryWrapper<Account> wrapper = new LambdaQueryWrapper<>();  // 创建Lambda风格的查询条件构造器
        // 如果有搜索关键词，则添加模糊搜索条件
        if (keyword != null && !keyword.isEmpty()) {  // 判断关键词是否为空
            wrapper.like(Account::getUserid, keyword)  // 模糊匹配用户名
                    .or().like(Account::getEmail, keyword)  // 或 模糊匹配邮箱
                    .or().like(Account::getPhone, keyword);  // 或 模糊匹配手机号
        }
        wrapper.orderByDesc(Account::getCreatedAt);  // 按创建时间降序排列（最新的排在前面）
        // 执行分页查询
        Page<Account> result = accountMapper.selectPage(page, wrapper);  // 使用分页对象和查询条件执行数据库查询
        // 将查询到的Account实体列表转换为UserVO视图对象列表（去掉敏感信息如密码）
        List<UserVO> voList = result.getRecords().stream()  // 获取查询结果的记录列表，并转换为Stream流
                .map(this::convertToVO)  // 对每条记录调用convertToVO方法进行转换
                .collect(Collectors.toList());  // 将Stream流收集为List集合
        // 返回封装好的分页结果
        return new PageResult<>(voList, result.getTotal(), pageNum, pageSize);  // 创建分页结果对象，包含数据列表、总条数、当前页码和每页大小
    }

    /**
     * 修改用户状态（管理员功能）
     * 启用或禁用指定用户的账号。
     *
     * @param userId 用户ID
     * @param status 目标状态（1=启用，0=禁用）
     * @throws IllegalArgumentException 当用户不存在时抛出异常
     */
    @Transactional  // 事务注解，确保状态修改操作的原子性
    public void updateUserStatus(Long userId, Integer status) {  // 修改用户状态方法，接收用户ID和目标状态
        // 根据ID查询用户
        Account account = accountMapper.selectById(String.valueOf(userId));  // 将Long类型的userId转为String后查询数据库
        if (account == null) {  // 查询结果为空
            throw new IllegalArgumentException("用户不存在");  // 抛出异常，提示用户不存在
        }
        account.setStatus(status);  // 设置新的状态值（1=启用，0=禁用）
        accountMapper.updateById(account);  // 根据主键更新用户记录
    }

    /**
     * 删除用户（管理员功能）
     * 根据用户ID删除指定用户。
     *
     * @param userId 用户ID
     */
    @Transactional  // 事务注解，确保删除操作的原子性
    public void deleteUser(Long userId) {  // 删除用户方法，接收用户ID
        accountMapper.deleteById(String.valueOf(userId));  // 将Long类型的userId转为String后，根据主键删除用户记录
    }

    /**
     * 将Account实体对象转换为UserVO视图对象
     * 使用Spring的BeanUtils.copyProperties进行属性复制，自动匹配同名字段。
     * 这样做可以避免将数据库实体（包含密码等敏感信息）直接返回给前端。
     *
     * @param account 用户账户实体对象（从数据库查询出来的原始数据）
     * @return 用户视图对象（VO），用于返回给前端展示
     */
    private UserVO convertToVO(Account account) {  // 私有方法，将Account实体转换为UserVO
        UserVO vo = new UserVO();  // 创建一个新的UserVO对象
        BeanUtils.copyProperties(account, vo);  // 使用Spring工具类将Account的属性值复制到UserVO中（只复制同名字段）
        return vo;  // 返回转换后的UserVO对象
    }
}
