use jni::objects::{JClass, JString};
use jni::sys::{jlong, jstring};
use jni::JNIEnv;
use std::{fs, i64};
use walkdir::WalkDir;

/// 获取指定文件夹的总大小，并将其格式化为字符串后返回给 Java
///
/// # 参数
/// - `env`: JNI 环境对象，用于与 Java 交互
/// - `_`: `JClass` 类型，表示调用此方法的 Java 类（未使用）
/// - `input`: Java 传递的字符串，代表文件夹路径
///
/// # 返回
/// - `jstring`: 表示文件夹大小的格式化字符串，如果发生错误，返回错误信息
#[no_mangle]
pub extern "system" fn Java_com_dazuoye_filemanager_utils_FSHelper_getFolderSizeNative<'local>(
    mut env: JNIEnv<'local>,
    _: JClass<'local>,
    input: JString<'local>,
) -> jstring {
    // 将 Java 字符串转换为 Rust 字符串
    let dir: String = env
        .get_string(&input)
        .expect("failed to parse input")
        .into();

    // 检查目录是否存在
    if !fs::exists(&dir).expect(format!("Cannot stat {dir}").as_str()) {
        // 如果目录不存在，返回相应的错误信息
        return env
            .new_string(format!("{} not exists!", dir))
            .expect("Couldn't create java string!")
            .into_raw();
    }
    // 从这里保证文件至少存在了

    let mut size: u64 = 0;
    // 使用 WalkDir 遍历目录中的所有文件和子目录
    for entry in WalkDir::new(dir) {
        match entry {
            Ok(item) => {
                // 尝试获取每个文件的元数据并累加其大小
                match item.metadata() {
                    Ok(metadata) => size += metadata.len(),
                    Err(e) => {
                        // 处理元数据获取失败的情况
                        eprintln!("Error getting metadata for item: {e:?}");
                        continue;
                    }
                }
            },
            Err(e) => {
                // 处理遍历目录时的错误
                eprintln!("Error walking directory: {:?}", e);
                continue;
            }
        }
    }

    // 将计算出的文件夹大小格式化为字符串并返回给 Java
    let output = env
        .new_string(format_size(size))
        .expect("Couldn't create java string!");

    output.into_raw()
}

/// 获取指定文件夹的总大小（以字节为单位），返回给 Java
///
/// # 参数
/// - `env`: JNI 环境对象，用于与 Java 交互
/// - `_`: `JClass` 类型，表示调用此方法的 Java 类（未使用）
/// - `input`: Java 传递的字符串，代表文件夹路径
///
/// # 返回
/// - `jlong`: 文件夹的总大小（以字节为单位），如果发生错误则返回 0
#[no_mangle]
pub extern "system" fn Java_com_dazuoye_filemanager_utils_FSHelper_getFolderSizeBytesNative<'local>(
    mut env: JNIEnv<'local>,
    _: JClass<'local>,
    input: JString<'local>,
) -> jlong {
    // 将 Java 字符串转换为 Rust 字符串
    let dir: String = env
        .get_string(&input)
        .expect("failed to parse input")
        .into();

    // 检查目录是否存在
    if !fs::exists(&dir).expect(format!("Cannot stat {}", dir).as_str()) {
        return 0;
    }
    // 从这里保证文件至少存在了

    let mut size: u64 = 0;
    // 使用 WalkDir 遍历目录中的所有文件和子目录
    for entry in WalkDir::new(dir) {
        match entry {
            Ok(item) => {
                // 尝试获取每个文件的元数据并累加其大小
                match item.metadata() {
                    Ok(metadata) => size += metadata.len(),
                    Err(e) => {
                        // 处理元数据获取失败的情况
                        eprintln!("Error getting metadata for item: {:?}", e);
                        continue;
                    }
                }
            },
            Err(e) => {
                // 处理遍历目录时的错误
                eprintln!("Error walking directory: {:?}", e);
                continue;
            }
        }
    }

    // 将文件夹大小转换为 `jlong`，如果溢出则返回 `i64::MAX`
    match i64::try_from(size) {
        Ok(compatible) => compatible,
        Err(_) => i64::MAX
    }
}

/// 格式化文件大小为更易读的字符串格式
///
/// # 参数
/// - `size`: 文件大小，以字节为单位
///
/// # 返回
/// - `String`: 格式化后的文件大小字符串（带单位，如 "KB", "MB" 等）
fn format_size(size: u64) -> String {
    // 定义单位
    let units = ["B", "KB", "MB", "GB", "TB", "PB"];
    let mut size = size as f64;
    let mut unit_index = 0;

    // 按 1024 递减计算，直到找到合适的单位
    while size >= 1024.0 && unit_index < units.len() - 1 {
        size /= 1024.0;
        unit_index += 1;
    }

    // 保留两位小数格式化输出
    format!("{:.2} {}", size, units[unit_index])
}
