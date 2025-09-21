document.addEventListener('DOMContentLoaded', function () {
    let dragSrcEl = null;

    // Delegated dragstart/dragend to ensure events fire even when inner elements are targeted
    document.addEventListener('dragstart', function (ev) {
        const todo = ev.target.closest && ev.target.closest('.todo');
        if (!todo) return;
        dragSrcEl = todo;
        ev.dataTransfer.setData('text/plain', todo.id);
        todo.classList.add('dragging');
        console.debug('dragstart', todo.id);
    });

    document.addEventListener('dragend', function (ev) {
        const todo = ev.target.closest && ev.target.closest('.todo');
        if (!todo) return;
        todo.classList.remove('dragging');
        console.debug('dragend', todo.id);
    });

    const cols = Array.from(document.querySelectorAll('.column'));
    const csrfMeta = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
    const csrfToken = csrfMeta ? csrfMeta.getAttribute('content') : null;
    const csrfHeader = csrfHeaderMeta ? csrfHeaderMeta.getAttribute('content') : 'X-CSRF-TOKEN';

    cols.forEach(col => {
        col.addEventListener('dragover', ev => ev.preventDefault());
        col.addEventListener('dragenter', ev => col.classList.add('drag-over'));
        col.addEventListener('dragleave', ev => col.classList.remove('drag-over'));
        col.addEventListener('drop', ev => {
            ev.preventDefault();
            col.classList.remove('drag-over');
            const id = ev.dataTransfer.getData('text/plain');
            if (!id) {
                console.debug('drop with no id data');
                return;
            }
            const el = document.getElementById(id);
            if (!el) {
                console.debug('drop: element not found', id);
                return;
            }

            const originalParent = el.parentElement;
            const nextSibling = el.nextSibling;

            // Optimistic UI update
            col.appendChild(el);

            // determine status from column id
            let status = 'TODO';
            if (col.id.includes('doing')) status = 'DOING';
            if (col.id.includes('completed')) status = 'COMPLETED';

            console.debug('sending status update', id, status);

            const headers = { 'Content-Type': 'application/json' };
            if (csrfToken) headers[csrfHeader] = csrfToken;

            fetch('/api/todos/' + id + '/status', {
                method: 'PATCH',
                headers: headers,
                body: JSON.stringify({ status: status })
            }).then(res => {
                if (!res.ok) {
                    // rollback UI change
                    if (nextSibling) originalParent.insertBefore(el, nextSibling);
                    else originalParent.appendChild(el);
                    res.text().then(t => console.error('Status update failed:', res.status, t));
                    alert('Status update failed: ' + res.status);
                }
            }).catch(err => {
                // network error: rollback
                if (nextSibling) originalParent.insertBefore(el, nextSibling);
                else originalParent.appendChild(el);
                console.error('Status update error', err);
                alert('Failed to update status (network error)');
            });
        });
    });
});
