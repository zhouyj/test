package com.feedss.app;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ButtonService {
	@Autowired
	ButtonRepository buttonRepository;

	// 返回按钮树
	public Map<String, Button> getAllButtonMap() {
		Map<String, Button> map = new LinkedHashMap<String, Button>();
		List<Button> buttons = buttonRepository.findByParentIdOrderBySort(null);
		for (Button button : buttons) {
			setChildrenButton(button);
			map.put(button.getPosition(), button);
		}
		return map;
	}

	// 返回按钮树
	public List<Button> getAllButtonList() {
		List<Button> buttons = buttonRepository.findByParentIdOrderBySort(null);
		for (Button button : buttons) {
			setChildrenButton(button);
		}
		return buttons;
	}

	// 遍历设置子按钮（菜单）
	private void setChildrenButton(Button button) {
		List<Button> bs = buttonRepository.findByParentIdOrderBySort(button.getUuid());
		if (bs != null && bs.size() > 0) {
			for (Button b : bs) {
				setChildrenButton(b);
			}
			button.setButtons(bs);
		}
	}
}
