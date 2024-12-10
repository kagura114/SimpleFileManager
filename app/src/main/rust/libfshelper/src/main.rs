use std::fs;
use walkdir::WalkDir;
fn main() {
    let dir: String = String::from(".");

    if !fs::exists(&dir).expect(format!("Cannot stat {}", dir).as_str()) {
        return;
    }
    // 从这里保证文件至少存在了

    let mut size: u64 = 0;
    for entry in WalkDir::new(dir) {
        match entry {
            Ok(item) => match item.metadata() {
                Ok(metadata) => size += metadata.len(),
                Err(e) => {
                    eprintln!("Error getting metadata for item: {:?}", e);
                    continue;
                }
            },
            Err(e) => {
                eprintln!("Error walking directory: {:?}", e);
                continue;
            }
        }
    }

    println!("{}", format_size(size))
}

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
