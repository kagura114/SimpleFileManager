use sysinfo::System;
extern crate page_size;

fn main() {
    println!("{}", page_size::get());
    let mut sys = System::new_all();
    // First we update all information of our `System` struct.
    sys.refresh_all();
    println!("=> system:");
    // RAM and swap information:
    println!("total memory: {} bytes", sys.total_memory());
    println!("used memory : {} bytes", sys.used_memory());
    println!("total swap  : {} bytes", sys.total_swap());
    println!("used swap   : {} bytes", sys.used_swap());
}
