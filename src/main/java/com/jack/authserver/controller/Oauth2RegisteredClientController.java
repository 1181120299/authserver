package com.jack.authserver.controller;

import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import javax.validation.constraints.NotNull;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;



import com.jack.authserver.entity.Oauth2RegisteredClient;
import com.jack.authserver.dto.Oauth2RegisteredClientDto;
import com.jack.authserver.service.Oauth2RegisteredClientService;

import com.jack.utils.web.R;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Objects;

/**
 * 
 * 
 * @author chenjiabao
 * @email 1181120299@qq.com
 * @date 2023-04-12 09:10:29
 */
@Validated
@Controller
@RequestMapping("/oauth2registeredclient")
public class Oauth2RegisteredClientController {

	@Resource
	private Oauth2RegisteredClientService oauth2RegisteredClientService;

	/**
	 * 返回列表页
	 */
	@GetMapping("/page")
	public String list(Model model,
					   @RequestParam(required = false, defaultValue = "1") Integer current,
					   @RequestParam(required = false, defaultValue = "10") Integer size) {
		IPage<Oauth2RegisteredClient> page = new Page<>(current, size);
		LambdaQueryWrapper<Oauth2RegisteredClient> wrapper = new LambdaQueryWrapper<Oauth2RegisteredClient>()
				.orderByDesc(Oauth2RegisteredClient::getClientIdIssuedAt)
				.orderByDesc(Oauth2RegisteredClient::getId);
		page = oauth2RegisteredClientService.page(page, wrapper);
		page.getRecords().forEach(client -> {
			client.setClientSecret(client.getClientSecret().substring(client.getClientSecret().indexOf("}") + 1));
		});

		model.addAttribute("page", page);
		return "index";
	}

	/**
	 * 返回新增页面
	 */
	@GetMapping("/toSave")
	public String toSave() {
		return "createClient";
	}

	/**
	 * 新增
	 */
	@PostMapping(value = "/save")
	@ResponseBody
	public R save(@RequestBody @Validated Oauth2RegisteredClientDto oauth2RegisteredClientDto){
		Oauth2RegisteredClient oauth2RegisteredClient = new Oauth2RegisteredClient();
		BeanUtils.copyProperties(oauth2RegisteredClientDto, oauth2RegisteredClient);
		oauth2RegisteredClientService.save(oauth2RegisteredClient);
		return R.ok("保存成功");
	}

	/**
	 * 返回更新页面
	 */
	@GetMapping("/toUpdate/{id}")
	public String toUpdate(@PathVariable("id") String id, Model model) {
		Oauth2RegisteredClient client = oauth2RegisteredClientService.getById(id);
		client.setClientSecret(client.getClientSecret().substring(client.getClientSecret().indexOf("}") + 1));
		model.addAttribute("client", client);
		return "updateClient";
	}

	/**
	 * 更新
	 * @param oauth2RegisteredClient
	 * @return
	 */
	@PostMapping("/update")
	@ResponseBody
	public R update(@RequestBody Oauth2RegisteredClient oauth2RegisteredClient){
		oauth2RegisteredClientService.updateById(oauth2RegisteredClient);

		return R.ok("修改成功");
	}

	/**
	 * 删除
	 */
	@GetMapping("/delete")
	@ResponseBody
	public R delete(@NotNull(message = "id不能为空") String id){
		oauth2RegisteredClientService.removeById(id);

		return R.ok("删除成功");
	}

	/**
	 * 检测应用名称是否以存在
	 * @param clientId	应用名称
	 * @return	retCode=2000表示应用名称可以使用，2999表示已经存在
	 */
	@GetMapping("/checkClientId")
	@ResponseBody
	public R checkClientId(@RequestParam String clientId) {
		Oauth2RegisteredClient existedClient = oauth2RegisteredClientService.getOne(new LambdaQueryWrapper<Oauth2RegisteredClient>()
				.eq(Oauth2RegisteredClient::getClientId, clientId));
		if (Objects.isNull(existedClient)) {
			return R.ok("可以使用");
		} else {
			return R.error("此应用名称已存在");
		}
	}


//	/**
//	 * 信息
//	 */
//	@GetMapping("/info/{id}")
//	public R info(@PathVariable("id") String id){
//		Oauth2RegisteredClient oauth2RegisteredClient = oauth2RegisteredClientService.getById(id);
//
//		return R.ok().setData(oauth2RegisteredClient);
//	}
//
//	/**
//	 * 保存
//	 */
//	@PostMapping("/save")
//	public R save(@RequestBody @Validated Oauth2RegisteredClientDto oauth2RegisteredClientDto){
//		Oauth2RegisteredClient oauth2RegisteredClient = new Oauth2RegisteredClient();
//		BeanUtils.copyProperties(oauth2RegisteredClientDto, oauth2RegisteredClient);
//		oauth2RegisteredClientService.save(oauth2RegisteredClient);
//
//		return R.ok();
//	}
//
//	/**
//	 * 修改
//	 */
//	@PostMapping("/update")
//	public R update(@RequestBody Oauth2RegisteredClient oauth2RegisteredClient){
//		oauth2RegisteredClientService.updateById(oauth2RegisteredClient);
//
//		return R.ok();
//	}
//
//	/**
//	 * 删除
//	 */
//	@GetMapping("/delete")
//	public R delete(@NotNull(message = "id不能为空") String id){
//		oauth2RegisteredClientService.removeById(id);
//
//		return R.ok();
//	}
//
//	/**
//	 * 列表
//	 */
//	@GetMapping("/page")
//	public R page(@RequestParam(required = false, defaultValue = "1") Integer current,
//				  @RequestParam(required = false, defaultValue = "10") Integer size) {
//		IPage<Oauth2RegisteredClient> page = new Page<>(current, size);
//		LambdaQueryWrapper<Oauth2RegisteredClient> wrapper = new LambdaQueryWrapper<Oauth2RegisteredClient>()
//				.orderByDesc(Oauth2RegisteredClient::getId);
//		page = oauth2RegisteredClientService.page(page, wrapper);
//
//		return R.ok().setData(page);
//	}
}
