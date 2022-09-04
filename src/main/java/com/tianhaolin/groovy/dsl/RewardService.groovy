package com.tianhaolin.groovy.dsl

class RewardService {
    //服务本身提供的代码桩和默认实现，以及一些标记 static boolean on_consume_provided = true

    static Binding baseBinding = new Binding();

    static {
        loadDSL(baseBinding)
        loadRewardRules(baseBinding)
    }

    /**
     * 与DSL的语义进行映射
     * @param binding
     */
    static void loadDSL(Binding binding) {

        /**
         * spec 是冗余字段，用于给市场人员对规则分类
         */
        binding.reward = { spec, closure ->
            // 用于支持链式解析, 上下文委派下去, 调用reward的闭包内在持有reward的上下文
            closure.delegate = delegate
            // 填充解析的业务逻辑，与规则定义无关
            binding.result = true
            binding.and = true
            closure()
        }

        /**
         * 满足 condition 才能发放奖励
         * 定义了规则有 且 / 或 的关系
         */
        binding.condition = { closure ->
            closure.delegate = delegate
            if (binding.and)
                binding.result = (closure() && binding.result)
            else
                binding.result = (closure() || binding.result)
        }

        binding.allOf = { closure ->
            def storeResult = binding.result
            def storeAnd = binding.and
            // 必须全满足才发放奖励，比较严格，先假设满足。用取非逻辑比较方便
            binding.result = true
            closure()
            if (storeAnd) {
                binding.result = (storeResult && binding.result)
            } else {
                binding.result = (storeResult || binding.result)
            }
            binding.and = storeAnd
        }

        binding.anyOf = { closure ->
            closure.delegate = delegate
            def storeResult = binding.result
            def storeAnd = binding.and
            // 有一个满足就发放奖励，比较宽松，先假设不满足。用取非逻辑比较方便
            binding.result = false
            closure()
            if (storeAnd) {
                binding.result = (storeResult && binding.result)
            } else {
                binding.result = (storeResult || binding.result)
            }
            binding.and = storeAnd
        }

        /**
         * 用 binding 来收集是否发放奖励的信息，借result变量来记录
         */
        binding.grant = { closure ->
            closure.delegate = delegate
            if (binding.result)
                closure()
        }

        /**
         * 发放奖励的一种定义，延长资源的时长
         */
        binding.extend = { days ->
            def bbPlus = new BroadbandPlus()
            bbPlus.extend(binding.account, binding.media, days)
        }

        /**
         * 发放奖励的一种定义，增加积分
         */
        binding.points = { points ->
            binding.account.points += points
        }

    }

    void prepareMedia(binding, media) {
        binding.media = media
        binding.isNewRelease = media.newRelease
        binding.isVideo = (media.type == "VIDEO")
        binding.isGame = (media.type == "GAME")
        binding.isSong = (media.type == "SONG")
    }

    /**
     *   初始化加载奖赏脚本，在这个脚本中，可以定义 onConsume 等 DSL
     *
     *   把预设的规则加进脚本中，静态确保每次都使用的是最新的规则
     */
    static void loadRewardRules(Binding binding) {
        Binding selfBinding = new Binding()
        GroovyShell shell = new GroovyShell(selfBinding)
        //市场人员写的 DSL 脚本
        shell.evaluate(new File("./rewards.groovy"))
        // onConsume 是约定的方法，并不是框架支持的。目的是将市场人员的规则反馈给调用者
        binding.onConsume = selfBinding.onConsume
    }

    void apply(account, media) {
        Binding binding = baseBinding;
        binding.account = account
        prepareMedia(binding,media)
        GroovyShell shell = new GroovyShell(binding)
        shell.evaluate("onConsume.delegate=this;onConsume()")
    }

}