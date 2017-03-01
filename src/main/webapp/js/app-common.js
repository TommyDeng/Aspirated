
/**
 * LocalStorage 操作
 */
// 以“key”为名称存储一个值“value”
function LocalStorage_set(key, value) {
	localStorage.setItem(key, value);
}

// 获取名称为“key”的值
function LocalStorage_get(key) {
	return localStorage.getItem(key);
}

// 枚举localStorage的方法：
function LocalStorage_printAll() {
	console.info('localStorage print');
	for (var i = 0; i < localStorage.length; i++) {
		var key = localStorage.key(i);
		var value = localStorage.key(i);
		var value = localStorage.getItem(key);
		console.info(key + ':' + value);
	}
}
// 删除名称为“key”的信息。
function LocalStorage_removeItem(key) {
	localStorage.removeItem(key);
}

// 清空localStorage中所有信息
function LocalStorage_clear() {
	localStorage.clear();
}

// mask page when <a><botton> data-page-mask set true
$(document).ready(function() {
	$("a[data-page-mask=true]").on('click', function() {
		$("#page-loading").show();
	});

	$("button[data-page-mask=true]").on('submit', function() {
		$("#page-loading").show();
	});
});