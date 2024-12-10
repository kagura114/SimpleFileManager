use jni::objects::JClass;
use jni::sys::jstring;
use jni::JNIEnv;
use sysinfo::System;

fn get_page_size() -> String {
    let page_size = page_size::get();
    return format!("{}K", page_size / 1024);
}

fn get_sys() -> String {
    let mut sys = System::new_all();
    sys.refresh_all();
    let kernel = System::kernel_version();
    // In mb (original result is in b)
    let all_memory = sys.total_memory()/1024/1024;
    let available_memory = sys.available_memory()/1024/1024;

    let kernel_string = match kernel {
        Some(k) => format!("Kernel: {}\n", k),
        None => String::new(),
    };

    format!(
        "{}Memory: {}MB available, {}MB in total",
        kernel_string, available_memory, all_memory
    )
}

#[no_mangle]
pub extern "system" fn Java_com_dazuoye_filemanager_utils_Sysinfo_getSystemNative<'local>(
    env: JNIEnv<'local>,
    _: JClass<'local>,
) -> jstring {
    let page_size = get_page_size();
    let output = env
        .new_string(format!("{}\nPage Size: {}", get_sys(), page_size))
        .expect("Couldn't create java string!");
    output.into_raw()
}
